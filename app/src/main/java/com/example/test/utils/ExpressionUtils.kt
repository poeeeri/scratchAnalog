package com.example.test.utils

import com.example.test.R
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.example.test.CodeBlockState
import com.example.test.CommandBlock
import com.example.test.IfBlockCommand
import com.example.test.VarBlockCommand
import com.example.test.Variable
import com.example.test.WhileBlockCommand
import java.util.Stack

fun evaluateIfCondition(
    leftExpression: String,
    rightExpression: String,
    comparisonOperator: String,
    vars: List<Variable>,
    context: Context
): Boolean {
    try {
        val leftRpn = convertToReversePolishNotation(leftExpression, context)
        val rightRpn = convertToReversePolishNotation(rightExpression, context)

        val leftValue = calculateArithmeticExpression(leftRpn, vars, context = context)
        val rightValue = calculateArithmeticExpression(rightRpn, vars, context = context)
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
    context: Context
) {
    commands.forEach { command ->
        when(command) {
            is VarBlockCommand-> {
                val index = vars.indexOfFirst { it.name == command.variable.name }
                if (index >= 0) {
                    vars[index]= vars[index].copy(expression = command.variable.expression)
                }
            }
            is IfBlockCommand -> {
                val cond = evaluateIfCondition(
                    command.ifBlock.leftExpression,
                    command.ifBlock.rightExpression,
                    command.ifBlock.comparisonOperator,
                    vars,
                    context
                )
                if (cond) {
                    executeIfCommands(command.ifBlock.commands, vars, context)
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
                        context
                    )) {
                    executeIfCommands(command.whileBlock.commands, vars, context)
                }
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

//преобразуем выражение в обратную польскую запись
fun convertToReversePolishNotation(expression: String, context: Context) : String{
    val output = StringBuilder()
    val stack = Stack<Char>()
    var i = 0

    if (expression.isBlank()){
        Toast.makeText(context, R.string.err_exp_is_blank, Toast.LENGTH_LONG).show()
    }

    while (i < expression.length) {
        val c = expression[i]

        when {
            c.isDigit()-> {
                while (i < expression.length && expression[i].isDigit()){
                    output.append(expression[i++])
                }
                output.append(' ')
                continue
            }

            c.isLetter()->{
                while (i <expression.length && (expression[i].isLetterOrDigit() || expression[i] == '_')){
                    output.append(expression[i++])
                }
                output.append(' ')
                continue
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

// вычисляем значение арифметического выражения
fun calculateArithmeticExpression(expression: String, vars: List<Variable>,  callStack: Set<String> = emptySet(), computedCache: MutableMap<String, Int> = mutableMapOf(), context: Context):Int{
    if (vars.any {it.name.isEmpty()}){
        Toast.makeText(context, R.string.err_var_with_enpty_name_found, Toast.LENGTH_LONG).show()
    }

    val stack = mutableListOf<Int>()
    val tokens = expression.split(" ").filter { it.isNotBlank() }

    if (tokens.isEmpty()){
        Toast.makeText(context, R.string.err_empty_exp, Toast.LENGTH_LONG).show()
    }

    for (token in tokens) {
        try {
            when {
                token.toIntOrNull() != null -> {
                    stack.add(token.toInt())
                }

                vars.any { it.name == token } -> {
                    val variable = vars.first {it.name == token}
                    val value = if (variable.expression.isBlank()){
                        0
                    } else{
                        calculateArithmeticExpression(variable.expression, vars, context=context)
                    }
                    stack.add(value)
                }

                token == "+" -> {
                    val b = stack.removeAt(stack.lastIndex)
                    val a = stack.removeAt(stack.lastIndex)
                    stack.add(a + b)
                }

                token == "-" -> {
                    val b = stack.removeAt(stack.lastIndex)
                    val a = stack.removeAt(stack.lastIndex)
                    stack.add(a - b)
                }

                token == "*" -> {
                    val b = stack.removeAt(stack.lastIndex)
                    val a = stack.removeAt(stack.lastIndex)
                    stack.add(a * b)
                }

                token == "/" -> {
                    val b = stack.removeAt(stack.lastIndex)
                    if (b == 0) {
                        Toast.makeText(context, R.string.err_div_by_zero, Toast.LENGTH_LONG).show()
                    }
                    val a = stack.removeAt(stack.lastIndex)
                    stack.add(a / b)
                }

                token == "%" -> {
                    val b = stack.removeAt(stack.lastIndex)
                    if (b == 0) {
                        Toast.makeText(context, R.string.err_div_by_zero, Toast.LENGTH_LONG).show()
                    }
                    val a = stack.removeAt(stack.lastIndex)
                    stack.add(a % b)
                }
            }
        } catch (e: NoSuchElementException){
            Toast.makeText(context, context.getString(R.string.err_var_token_not_found, token), Toast.LENGTH_LONG).show()
        }
    }

    val result = stack.singleOrNull()
    if (result == null){
        Toast.makeText(context, R.string.err_invalid_exp_format, Toast.LENGTH_LONG).show()
        return 0
    }
    return result
}

//находим зависимость переменной от других переменных
fun extractDependencies(expression: String): Set<String>{
    return Regex("[a-zA-Z_]\\w*")
        .findAll(expression)
        .map { it.value }
        .toSet()
}
// здесь мы пересчитываем все переменные
fun recalculateAllVariables(vars: List<Variable>, context: Context) : Result<List<Variable>> {
    if (vars.isEmpty()) return Result.success(emptyList())

    val graph = mutableMapOf<String, Set<String>>()
    for (variable in vars) {
        graph[variable.name] = extractDependencies(variable.expression)
    }

    return runCatching {
        val updatedVars = vars.map { it.copy() }.toMutableList()
        val sortedOrder = topologicalSort(graph)
        val computedValues = mutableMapOf<String, Int>()
        for (varName in sortedOrder) {
            val variable = updatedVars.first { it.name == varName }
            try {
                var processed = variable.expression
                computedValues.forEach { (name, value) ->
                    processed = processed.replace(name, value.toString())
                }
                val rpn = convertToReversePolishNotation(processed, context)
                val value = calculateArithmeticExpression(
                    rpn,
                    vars.filter { computedValues.containsKey(it.name) },
                    context = context
                )
                computedValues[varName] = value
                updatedVars.replaceAll {
                    if (it.name == varName)
                        it.copy(expression = value.toString())
                    else
                        it
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
    val result = recalculateAllVariables(state.vars, context)
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
                context
            )
            if (conditionRes)
                executeIfCommands(block.commands, state.vars, context)
        }
        // и с циклом while
        state.whileBlocks.forEach {
            block -> executeIfCommands(
                listOf(WhileBlockCommand(block)),
                state.vars,
                context
            )
        }
    }.
    onFailure { e ->
        Toast.makeText(context, e.message ?: "Error", Toast.LENGTH_LONG).show()
    }
}

// проверка на валидность арифм операций со скобками
fun isValidArithmExpression(state: CodeBlockState) : Boolean {
    state.assignmentArithmExpr = rewriteExpression(state.assignmentArithmExpr)
    var lvl = 0;

    // проверяем на скобочные пары
    for (char in state.assignmentArithmExpr) {
        when(char) {
            '(' -> lvl++
            ')' -> if (--lvl < 0)
                return false
        }
    }
    if (lvl != 0) return false

    val regex = Regex("[A-Za-z_]\\w*|\\d+(?:\\.\\d+)?|[()+\\-%*/]")
    val tokens = regex.findAll(state.assignmentArithmExpr).map { it.value }.toList()
    if (tokens.isEmpty()) return false

    // машина состояний которая ждет после числа/переменной оператор и наоборот
    var expect = true
    for (t in tokens) {
        if (expect) {
            when {
                t.matches(Regex("\\d+")) || t.matches(Regex("(?!_|\\d+)([a-zA-Z_]\\w*)"))
                    -> expect = false

                t == "(" -> expect = true
                t == "+" || t == "-" -> expect = true
                else -> return false
            }
        }
        else {
            when (t) {
                "+", "-", "/", "%", "*" -> expect = true
                ")" -> expect = false
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