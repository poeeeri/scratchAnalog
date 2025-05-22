package com.example.test.utils

import android.Manifest
import com.example.test.R
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.example.test.ArrayBlock
import com.example.test.ArrayBlockCommand
import com.example.test.CodeBlockState
import com.example.test.CommandBlock
import com.example.test.IfBlockCommand
import com.example.test.VarBlockCommand
import com.example.test.Variable
import com.example.test.WhileBlockCommand
import java.util.Stack
import kotlin.math.exp

fun evaluateIfCondition(
    leftExpression: String,
    rightExpression: String,
    comparisonOperator: String,
    vars: List<Variable>,
    context: Context,
    arrays: List<ArrayBlock> = emptyList()
): Boolean {
    try {
        val leftRpn = convertToReversePolishNotation(leftExpression, context)
        val rightRpn = convertToReversePolishNotation(rightExpression, context)

        val leftValue = calculateArithmeticExpression(leftRpn, vars, context = context, arrays = arrays)
        val rightValue = calculateArithmeticExpression(rightRpn, vars, context = context, arrays = arrays)
        return when (comparisonOperator) {
            "==" -> leftValue == rightValue
            "!=" -> leftValue != rightValue
            ">" -> leftValue > rightValue
            "<" -> leftValue < rightValue
            ">=" -> leftValue >= rightValue
            "<=" -> leftValue <= rightValue
            else -> {
                Toast.makeText(
                    context,
                    context.getString(R.string.err_invalid_comparison_operator, comparisonOperator),
                    Toast.LENGTH_LONG
                ).show()
                false
            }
        }
    }
    catch (e: Exception) {
        Toast.makeText(
            context,
            context.getString(R.string.err_eval_condition, e.message),
            Toast.LENGTH_LONG
        ).show()
        return false
    }
}

// проходится по командам по порядку их добавления и вложенности и присваивает переменной
fun executeIfCommands(
    commands: List<CommandBlock>,
    vars: SnapshotStateList<Variable>,
    context: Context,
    arrays: List<ArrayBlock> = emptyList()
) {
    commands.forEach { command ->
        when(command) {
            is VarBlockCommand-> {
                val variable = command.variable
                val expr = variable.expression

                val arraySetPattern = Regex("([a-zA-Z_]\\w*)\\[(.*?)\\]\\s*=\\s*(.*)")
                val match = arraySetPattern.matchEntire(expr)

                if (match != null) {
                    val arrName = match.groupValues[1]
                    val idExpr = match.groupValues[2]
                    val valueExpr = match.groupValues[3]

                    setArrayElement(
                        arrName,
                        idExpr,
                        valueExpr,
                        arrays.toMutableList(),
                        vars,
                        context
                    )
                }
                else {
                    val index = vars.indexOfFirst { it.name == command.variable.name }
                    if (index >= 0) {
                        vars[index] = vars[index].copy(expression = command.variable.expression)
                    }
                }
            }
            is IfBlockCommand -> {
                val cond = evaluateIfCondition(
                    command.ifBlock.leftExpression,
                    command.ifBlock.rightExpression,
                    command.ifBlock.comparisonOperator,
                    vars,
                    context,
                    arrays
                )
                if (cond) {
                    executeIfCommands(command.ifBlock.commands, vars, context, arrays)
                }
            }

            is WhileBlockCommand -> {
                Log.d("EXEC", "while-condition: ${command.whileBlock.leftExpression} ${command.whileBlock.comparisonOperator} ${command.whileBlock.rightExpression}")
                Log.d("EXEC", "while-body size = ${command.whileBlock.commands.size}")

                while (evaluateIfCondition(
                        command.whileBlock.leftExpression,
                        command.whileBlock.rightExpression,
                        command.whileBlock.comparisonOperator,
                        vars,
                        context,
                        arrays
                    )) {
                    executeIfCommands(command.whileBlock.commands, vars, context, arrays)
                }
            }

            is ArrayBlockCommand -> {
                val arrayId = arrays.indexOfFirst { it.name == command.arrayBlock.name }
                if (arrayId >= 0)
                    Log.d("EXEC", "Processing array block: ${command.arrayBlock.name}")
            }
        }
    }
}

//приоритеты операций
fun getPriority(operator: Char): Int = when(operator){
    '+','-' -> 1
    '*','/', '%' -> 2
    else -> 0
}

// Предварительно обрабатываем выражение с массивами
fun preprocessArrayExpression(expression: String) : String {
    val arrPattern = Regex("([a-zA-Z_]\\w*)\\s*\\[(.*?)\\]")

    var res = expression
    var offset = 0
    arrPattern.findAll(expression).forEach { matchRes ->
        val arrName = matchRes.groupValues[1]
        val idExpr = matchRes.groupValues[2].trim()

        val arrayToken = "${arrName}[${idExpr}]"
        val startPos = matchRes.range.first + offset
        val endPos = matchRes.range.last + offset + 1
        res = res.substring(0, startPos) + arrayToken + res.substring(endPos)
        offset += arrayToken.length - (endPos - startPos)
    }
    return res
}

//преобразуем выражение в обратную польскую запись
fun convertToReversePolishNotation(expression: String, context: Context) : String{
    val preprocessedExpr = preprocessArrayExpression(expression)
    val processedExpr = rewriteExpression(preprocessedExpr)

    val output = StringBuilder()
    val stack = Stack<Char>()
    var i = 0

    if (processedExpr.isBlank()){
        Toast.makeText(context, R.string.err_exp_is_blank, Toast.LENGTH_LONG).show()
    }

    while (i < processedExpr.length) {
        val c = processedExpr[i]

        when {
            c.isDigit()-> {
                while (i < processedExpr.length && processedExpr[i].isDigit()){
                    output.append(processedExpr[i++])
                }
                output.append(' ')
                continue
            }

            c.isLetter() || c == '_' ->{
                val start = i
                while (i <processedExpr.length && (processedExpr[i].isLetterOrDigit() || processedExpr[i] == '_')){
                    output.append(processedExpr[i++])
                }
                if (i < processedExpr.length && processedExpr[i] == '[') {
                    output.append('[')
                    i++

                    var bracketLevel = 1
                    while (i < processedExpr.length && bracketLevel > 0) {
                        if (processedExpr[i] == '[') bracketLevel++
                        else if (processedExpr[i] == ']') bracketLevel--

                        if (bracketLevel > 0) output.append(processedExpr[i++])
                        else {
                            output.append(']')
                            i++
                        }
                    }
                }
                output.append(' ')
                continue
            }

            c == '[' -> {
                output.append("[ ")
                i++
            }

            c == ']' -> {
                output.append("] ")
                i++
            }

            c == '(' -> {
                stack.push(c)
                i++
            }

            c == ')' ->{
                while (stack.isNotEmpty() && stack.peek() != '('){
                    output.append(stack.pop()).append(' ')
                }
                if (stack.isEmpty() || stack.peek() != '('){
                    Toast.makeText(context, R.string.err_extra_closing_parenthesis, Toast.LENGTH_LONG).show()
                }
                stack.pop()
                i++
            }

            c in "+-*/%" ->{
                while (stack.isNotEmpty() && stack.peek() != '(' && getPriority(stack.peek()) >= getPriority(c)){
                    output.append(stack.pop()).append(' ')
                }
                stack.push(c)
                i++
            }

            c.isWhitespace()-> {
                i++
            }

            else-> {
                Toast.makeText(context, context.getString(R.string.err_invalid_character, c), Toast.LENGTH_LONG).show()
                break
            }
        }
    }

    while(stack.isNotEmpty()){
        val operator = stack.pop()
        when (operator){
            '(' -> {
                Toast.makeText(context, R.string.extra_opening_parenthesis, Toast.LENGTH_LONG).show()
            }
            ')' -> {
                Toast.makeText(context, R.string.extra_closing_parenthesis, Toast.LENGTH_LONG).show()
            }
        }
        output.append(operator).append(' ')
    }
    return output.toString().trim().also{
        if (it.isEmpty()) {
            Toast.makeText(context, R.string.empty_rpn_exp, Toast.LENGTH_LONG).show()
        }
    }
}

fun getArrayElementValue(
    arrName: String,
    indexExpression: String,
    arrays: List<ArrayBlock>,
    vars: List<Variable>,
    context: Context
): Int {
    try {
        val mas = arrays.firstOrNull { it.name == arrName }
        if (mas == null) {
            Toast.makeText(
                context,
                "Array not found: $arrName",
                Toast.LENGTH_LONG
            ).show()
            return 0
        }

        val idRpn = convertToReversePolishNotation(indexExpression, context)
        val i = calculateArithmeticExpression(idRpn, vars, context = context, arrays = arrays)
        if (i < 0 || i >= mas.size) {
            Toast.makeText(
                context,
                "Array index out of bounds: $i",
                Toast.LENGTH_LONG
            ).show()
            return 0
        }
        return mas.elems.getOrNull(i)?.toIntOrNull() ?: 0
    }
    catch (e: Exception) {
        Toast.makeText(
            context,
            "Error accessing array element: ${e.message}",
            Toast.LENGTH_LONG
        ).show()
        return 0
    }
}

fun setArrayElement(
    arrName: String,
    indexExpression: String,
    valueExpression: String,
    arrays: MutableList<ArrayBlock>,
    vars: List<Variable>,
    context: Context
): Boolean {
    try {
        val mas = arrays.find { it.name == arrName }
        if (mas == null) {
            Toast.makeText(
                context,
                "Array $arrName not found",
                Toast.LENGTH_LONG
            ).show()
            return false
        }
        val idRpn = convertToReversePolishNotation(indexExpression, context)
        val i = calculateArithmeticExpression(idRpn, vars, context = context, arrays = arrays)
        if (i < 0 || i >= mas.size) {
            Toast.makeText(
                context,
                "Array index out of range: $i for array ${mas.name} of size ${mas.size}",
                Toast.LENGTH_LONG
            ).show()
            return false
        }
        val valueRpn = convertToReversePolishNotation(valueExpression, context)
        val value = calculateArithmeticExpression(valueRpn, vars, context = context, arrays = arrays)

        val id = arrays.indexOf(mas)
        val newElems = mas.elems.toMutableList()
        newElems[i] = value.toString()
        arrays[id] = mas.copy(elems = newElems)
        return true
    }
    catch (e: Exception) {
        Toast.makeText(
            context,
            "Error setting array element: ${e.message}",
            Toast.LENGTH_LONG
        ).show()
        return false
    }
}

// вычисляем значение арифметического выражения
fun calculateArithmeticExpression(
    expression: String,
    vars: List<Variable>,
    callStack: Set<String> = emptySet(),
    computedCache: MutableMap<String, Int> = mutableMapOf(),
    context: Context,
    arrays: List<ArrayBlock> = emptyList()
): Int{
    if (vars.any {it.name.isEmpty()}){
        Toast.makeText(context, R.string.err_var_with_enpty_name_found, Toast.LENGTH_LONG).show()
    }

    val arrayAccessPattern = Regex("([a-zA-Z_]\\w*)\\[(.*)\\]")

    val stack = mutableListOf<Int>()
    val tokens = expression.split(" ").filter { it.isNotBlank() }
    Log.d("CalcExpr", "Tokens: $tokens")

    if (tokens.isEmpty()){
        Log.e("CalcExpr", "Empty expression")
        Toast.makeText(context, R.string.err_empty_exp, Toast.LENGTH_LONG).show()
        return 0
    }

    for (token in tokens) {
        try {
            val arrMatch = arrayAccessPattern.matchEntire(token)
            when {
                arrMatch != null -> {
                    val arrName = arrMatch.groupValues[1]
                    val idExpr = arrMatch.groupValues[2]

                    val mas = arrays.firstOrNull { it.name == arrName }
                    if (mas != null) {
                        val idRpn = convertToReversePolishNotation(idExpr, context)
                        val i = calculateArithmeticExpression(idRpn, vars, context = context, arrays = arrays)

                        if (i >= 0 && i < mas.size) {
                            val value = mas.elems.getOrNull(i)?.toIntOrNull() ?: 0
                            stack.add(value)
                        }
                        else {
                            Toast.makeText(
                                context,
                                "Array index out of bounds: $i",
                                Toast.LENGTH_LONG
                            ).show()
                            stack.add(0)
                        }
                    }
                    else {
                        Log.e("CalcExpr", "Array not found: $arrName")
                        Toast.makeText(
                            context,
                            "Array not found: $arrName",
                            Toast.LENGTH_LONG
                        ).show()
                        stack.add(0)
                    }
                }

                token.toIntOrNull() != null -> {
                    Log.d("CalcExpr", "Numeric token: $token")
                    stack.add(token.toInt())
                }

                vars.any { it.name == token } -> {
                    val variable = vars.first {it.name == token}
                    Log.d("CalcExpr", "Variable token: ${variable.name} = ${variable.expression}")
                    val value = if (variable.expression.isBlank()){
                        0
                    } else{
                        val rpn = convertToReversePolishNotation(variable.expression, context)
                        Log.d("CalcExpr", "Variable expression RPN: $rpn")
                        calculateArithmeticExpression(rpn, vars, context=context)
                    }
                    Log.d("CalcExpr", "Variable value: $value")
                    stack.add(value)
                }

                token == "+" -> {
                    val b = stack.removeAt(stack.lastIndex)
                    val a = stack.removeAt(stack.lastIndex)
                    Log.d("CalcExpr", "Operation: $a + $b = ${a + b}")
                    stack.add(a + b)
                }

                token == "-" -> {
                    val b = stack.removeAt(stack.lastIndex)
                    val a = stack.removeAt(stack.lastIndex)
                    Log.d("CalcExpr", "Operation: $a - $b = ${a - b}")
                    stack.add(a - b)
                }

                token == "*" -> {
                    val b = stack.removeAt(stack.lastIndex)
                    val a = stack.removeAt(stack.lastIndex)
                    Log.d("CalcExpr", "Operation: $a * $b = ${a * b}")
                    stack.add(a * b)
                }

                token == "/" -> {
                    val b = stack.removeAt(stack.lastIndex)
                    if (b == 0) {
                        Log.e("CalcExpr", "Division by zero")
                        Toast.makeText(context, R.string.err_div_by_zero, Toast.LENGTH_LONG).show()
                    }
                    val a = stack.removeAt(stack.lastIndex)
                    Log.d("CalcExpr", "Operation: $a / $b = ${a / b}")
                    stack.add(a / b)
                }

                token == "%" -> {
                    val b = stack.removeAt(stack.lastIndex)
                    if (b == 0) {
                        Log.e("CalcExpr", "Modulo by zero")
                        Toast.makeText(context, R.string.err_div_by_zero, Toast.LENGTH_LONG).show()
                    }
                    val a = stack.removeAt(stack.lastIndex)
                    Log.d("CalcExpr", "Operation: $a % $b = ${a % b}")
                    stack.add(a % b)
                }
            }
            Log.d("CalcExpr", "Stack after token: $stack")
        }
        catch (e: NoSuchElementException){
            Log.e("CalcExpr", "Error processing token $token: ${e.message}", e)
            Toast.makeText(context, context.getString(R.string.err_var_token_not_found, token), Toast.LENGTH_LONG).show()
        }
        catch (e: Exception) {
            Log.e("CalcExpr", "Unexpected error processing token $token: ${e.message}", e)
            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            return 0
        }
    }

    val result = stack.singleOrNull()
    if (result == null){
        Log.e("CalcExpr", "Invalid expression format. Stack: $stack")
        Toast.makeText(context, R.string.err_invalid_exp_format, Toast.LENGTH_LONG).show()
        return 0
    }
    Log.d("CalcExpr", "Final result: $result")
    return result
}

//находим зависимость переменной от других переменных
fun extractDependencies(expression: String): Set<String>{
    val varPattern = Regex("[a-zA-Z_]\\w*")
    val arrPattern = Regex("([a-zA-Z_]\\w*)\\s*\\[")

    val vars = varPattern.findAll(expression).map { it.value }.toSet()
    val arrVars = arrPattern.findAll(expression).map { it.groupValues[1] }.toSet()
    return vars + arrVars
}
// здесь мы пересчитываем все переменные
fun recalculateAllVariables(
    vars: List<Variable>,
    context: Context,
    arrays: List<ArrayBlock> = emptyList()
) : Result<List<Variable>> {
    if (vars.isEmpty()) return Result.success(emptyList())

    val graph = mutableMapOf<String, Set<String>>()
    for (variable in vars) {
        graph[variable.name] = extractDependencies(variable.expression)
    }
    for (array in arrays) {
        graph[array.name] = emptySet()
    }

    return runCatching {
        val updatedVars = vars.map { it.copy() }.toMutableList()
        val sortedOrder = topologicalSort(graph)
        val computedValues = mutableMapOf<String, Int>()

        val arrayNames = arrays.map { it.name }.toSet()
        for (varName in sortedOrder) {
            if (arrayNames.contains(varName)) continue

            val variable = updatedVars.firstOrNull { it.name == varName }
            if (variable == null) {
                Log.e("RecalculateVars", "Variable $varName not found in collection")
                Toast.makeText(
                    context,
                    "Variable $varName not found",
                    Toast.LENGTH_LONG
                ).show()
                continue
            }
            try {
                val arrayAccessPattern = Regex("([a-zA-Z_]\\w*)\\[(.*?)\\]")
                val arrMatch = arrayAccessPattern.matchEntire(variable.expression.trim())
                if (arrMatch != null) {
                    val arrName = arrMatch.groupValues[1]
                    val idExpr = arrMatch.groupValues[2]
                    val value = getArrayElementValue(
                        arrName,
                        idExpr,
                        arrays,
                        updatedVars,
                        context
                    )
                    computedValues[varName] = value
                    updatedVars.replaceAll {
                        if (it.name == varName) it.copy(expression = value.toString())
                        else it
                    }
                }
                else {
                    var processed = variable.expression
                    computedValues.forEach { (name, value) ->
                        processed = processed.replace(name, value.toString())
                    }
                    val rpn = convertToReversePolishNotation(processed, context)
                    val value = calculateArithmeticExpression(
                        rpn,
                        vars.filter { computedValues.containsKey(it.name) },
                        context = context,
                        arrays = arrays
                    )
                    computedValues[varName] = value
                    updatedVars.replaceAll {
                        if (it.name == varName)
                            it.copy(expression = value.toString())
                        else
                            it
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(context, "${e.message}", Toast.LENGTH_LONG).show()
            }
        }
        updatedVars
    }
}

//используем сортировку переменных, чтобы считать их в правильном порядке, если они связаны между собой
fun dfs(graph: Map<String, Set<String>>, node: String, visited: MutableMap<String, Int>, order: MutableList<String>){
    when (visited[node]){
        1 -> throw IllegalArgumentException("Cyclic dependency")
        2 -> return
    }

    visited[node] = 1
    graph[node]?.forEach{
            neighbor -> dfs(graph, neighbor, visited, order)
    }

    visited[node] = 2
    order.add(node)
}

fun topologicalSort(graph: Map<String, Set<String>>) : List<String> {
    val visited = mutableMapOf<String, Int>()
    val order = mutableListOf<String>()

    for (node in graph.keys){
        if (visited[node] == null){
            dfs(graph, node, visited, order)
        }
    }
    return order
}

fun recCalAll(state: CodeBlockState, context: Context) {
    val result = recalculateAllVariables(state.vars, context, state.arrays)
    result.onSuccess { updated ->
        state.vars.clear()
        state.vars.addAll(updated)
        // Также с условиями!
        state.ifBlock.forEach { block ->
            val conditionRes = evaluateIfCondition(
                block.leftExpression,
                block.rightExpression,
                block.comparisonOperator,
                state.vars,
                context,
                state.arrays
            )
            if (conditionRes)
                executeIfCommands(block.commands, state.vars, context, state.arrays)
        }
        // и с циклом while
        state.whileBlocks.forEach {
            block -> executeIfCommands(
                listOf(WhileBlockCommand(block)),
                state.vars,
                context,
                state.arrays
            )
        }
    }.
    onFailure { e ->
        Log.e("RecCalAll", "Error recalculating variables: ${e.message}", e)
        Toast.makeText(context, e.message ?: "Error", Toast.LENGTH_LONG).show()
    }
}

// проверка на валидность арифм операций со скобками
fun isValidArithmExpression(state: CodeBlockState) : Boolean {
    val processedExpr = preprocessArrayExpression(state.assignmentArithmExpr)
    state.assignmentArithmExpr = rewriteExpression(processedExpr)
    var lvl = 0
    var bracketLevel = 0

    // проверяем на скобочные пары
    for (char in state.assignmentArithmExpr) {
        when(char) {
            '(' -> lvl++
            ')' -> if (--lvl < 0)
                return false
            '[' -> bracketLevel++
            ']' -> if (--bracketLevel < 0)
                return false
        }
    }
    if (lvl != 0 || bracketLevel != 0) return false

    val regex = Regex("[A-Za-z_]\\w*(?:\\[(?:[^\\[\\]]+)\\])?|\\d+(?:\\.\\d+)?|[()+\\-%*/\\[\\]]")
    val tokens = regex.findAll(state.assignmentArithmExpr).map { it.value }.toList()
    if (tokens.isEmpty()) return false

    // машина состояний которая ждет после числа/переменной оператор и наоборот
    var expect = true
    for (t in tokens) {
        if (expect) {
            when {
                t.matches(Regex("\\d+")) || t.matches(Regex("(?!_|\\d+)([a-zA-Z_]\\w*)")) ||
                        t.matches(Regex("(?!_|\\d+)([a-zA-Z_]\\w*)\\[.*\\]"))
                    -> expect = false

                t == "(" -> expect = true
                t == "[" -> expect = true
                t == "+" || t == "-" -> expect = true
                else -> return false
            }
        }
        else {
            when (t) {
                "+", "-", "/", "%", "*" -> expect = true
                ")" -> expect = false
                "]" -> expect = false
                else -> return false
            }
        }
    }
    return !expect
}

// перезаписываем такую запись как например 2(1+3) в 2*(1+3)
fun rewriteExpression(expression: String) : String {
    var str = expression
    str = Regex("([A-Za-z_]\\w*|\\d+)\\s*\\(").replace(str) { m ->
        "${m.groupValues[1]}*("
    }

    str = Regex("(\\d+(?:\\.\\d+)?)([A-Za-z_]\\w*)").replace(str) { m ->
        "${m.groupValues[1]}*${m.groupValues[2]}"
    }
    return str;
}