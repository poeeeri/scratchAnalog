package com.example.test.utils

import android.annotation.SuppressLint
import com.example.test.R
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import com.example.test.ArrayBlock
import com.example.test.ArrayBlockCommand
import com.example.test.CodeBlockState
import com.example.test.CommandBlock
import com.example.test.ForBlock
import com.example.test.ForBlockCommand
import com.example.test.IfBlockCommand
import com.example.test.VarBlockCommand
import com.example.test.Variable
import com.example.test.VariableType
import com.example.test.WhileBlockCommand
import java.util.Stack

@SuppressLint("DefaultLocale")
fun formatNumber(value: Double) : String {
    return if (value == value.toInt().toDouble())
        value.toInt().toString()
    else value.toString()
}

fun checkTypeCompatibility(variable: Variable, value: Double, context: Context) : Boolean {
    if (variable.type == VariableType.INT && value != value.toInt().toDouble()) {
        Toast.makeText(
            context,
            context.getString(R.string.cannot_convert_float_to_int),
            Toast.LENGTH_LONG
        ).show()
        return false
    }
    return true
}

fun evaluateIfCondition(
    leftExpression: String,
    rightExpression: String,
    comparisonOperator: String,
    state: CodeBlockState,
    context: Context,
    arrays: List<ArrayBlock> = emptyList()
): Boolean {
    try {
        val leftRpn = convertToReversePolishNotation(leftExpression, context)
        val rightRpn = convertToReversePolishNotation(rightExpression, context)

        Log.d("EXEC", "Left RPN: $leftRpn")
        Log.d("EXEC", "Right RPN: $rightRpn")


        val leftValue : Double = calculateArithmeticExpression(leftRpn, state, context = context, arrays = arrays)

        val rightValue : Double = calculateArithmeticExpression(rightRpn,  state, context = context, arrays = arrays)

        Log.d("EXEC", "Left value: $leftValue, Right value: $rightValue")

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
fun executeCommands(
    commands: List<CommandBlock>,
    state: CodeBlockState,
    context: Context,
    arrays: List<ArrayBlock> = emptyList()
) {
    commands.forEach { command ->
        when(command) {
            is IfBlockCommand -> {
                val cond = evaluateIfCondition(
                    command.ifBlock.leftExpression,
                    command.ifBlock.rightExpression,
                    command.ifBlock.comparisonOperator,
                    state,
                    context,
                    arrays
                )
                if (cond) {
                    executeCommands(command.ifBlock.commands, state, context, state.arrays)
                } else {
                    executeCommands(command.ifBlock.elseCommands, state, context, state.arrays)
                }
            }

            is VarBlockCommand-> {
                val variable = command.variable
                val expr = variable.expression

                val arraySetPattern = Regex("([a-zA-Z_]\\w*)\\[(.*?)\\]\\s*=\\s*(.*)")
                val match = arraySetPattern.matchEntire(expr)

                if (match != null) {
                    val arrName = match.groupValues[1]
                    val idExpr = match.groupValues[2]
                    val valueExpr = match.groupValues[3]
                    Log.d("EXEC", "Setting array element: $arrName[$idExpr] = $valueExpr")
                    val success = setArrayElement(
                        arrName,
                        idExpr,
                        valueExpr,
                        state.arrays,
                        state,
                        context
                    )

                    if (success) Log.d("EXEC", "Array element set successfully")
                    else Log.d("EXEC", "Failed to set array element")
                }
                else {
                    val varName = variable.name
                    val rpn = convertToReversePolishNotation(variable.expression, context)
                    val result = calculateArithmeticExpression(rpn, state, context = context,  arrays = arrays)

                    val index = state.vars.indexOfFirst { it.name == varName }
                    if (index >= 0) {
                        state.vars[index] = state.vars[index].copy(expression = formatNumber(result), value = result)
                        Log.d("EXEC", "Updated existing variable $varName = $result")
                    }
                    else {
                        val newVar = Variable(
                            name = varName,
                            expression = formatNumber(result),
                            value = result,
                            pos = IntOffset(0, state.vars.size * 60)
                        )
                        state.vars.add(newVar)
                        Log.d("EXEC", "Created new variable $varName = $result")
                    }
                }
            }

            is WhileBlockCommand -> {
                while (evaluateIfCondition(
                        command.whileBlock.leftExpression,
                        command.whileBlock.rightExpression,
                        command.whileBlock.comparisonOperator,
                        state,
                        context,
                        arrays
                    )) {
                    executeCommands(command.whileBlock.commands, state, context, state.arrays)

                    val recalcResult = recalculateAllVariables(state, context, state.arrays)
                    recalcResult.onSuccess { updated ->
                        state.vars.clear()
                        state.vars.addAll(updated)
                    }.onFailure { e ->
                        Toast.makeText(
                            context,
                            e.message ?: context.getString(R.string.error),
                            Toast.LENGTH_LONG
                        ).show()
                        return
                    }
                }
            }

            is ArrayBlockCommand -> {
                val arrayId = arrays.indexOfFirst { it.name == command.arrayBlock.name }
                if (arrayId >= 0)
                    Log.d("EXEC", "Processing array block: ${command.arrayBlock.name}")
            }

            is ForBlockCommand -> {
                val block = command.forBlock
                val initStr = block.startExpression
                val index = state.vars.indexOfFirst { it.name == block.variable }
                val rpnEndExpression = convertToReversePolishNotation(block.endExpression, context)
                val calculEndExpr = calculateArithmeticExpression(
                    rpnEndExpression, state, context, arrays
                )

                if (block.doCommands.isNotEmpty()) {
                    executeCommands(block.doCommands.toList(), state, context, arrays)
                }
//                val recalcResult = recalculateAllVariables(state, context, arrays)
//                recalcResult.onSuccess { updated ->
//                    state.vars.clear()
//                    state.vars.addAll(updated)
//                }.onFailure { e ->
//                    Toast.makeText(context, e.message ?: "Error", Toast.LENGTH_LONG).show()
//                    return
//                }
                if (index < 0) {
                    state.vars.add(
                        Variable(
                            name = block.variable,
                            expression = initStr,
                            type = VariableType.INT
                        )
                    )
                }
                else {
                    state.vars[index] = state.vars[index].copy(expression = initStr)
                }

                var curLoopValue = initStr.toDoubleOrNull() ?: 0.0
                while (when(block.comparisonOperator) {
                        "<" ->  currentValue(state.vars, state, block, context, arrays) < calculEndExpr
                        "<=" ->  currentValue(state.vars, state, block, context, arrays) <= calculEndExpr
                        ">" ->  currentValue(state.vars, state, block, context, arrays) > calculEndExpr
                        ">=" ->  currentValue(state.vars, state, block, context, arrays) >= calculEndExpr
                        else -> false
                }) {
                    executeCommands(block.commands.toList(), state, context, arrays)
                    writeCurrValue(currentValue(state.vars, state, block, context, arrays) + block.stepIter, state.vars, index)
                }
            }
        }
    }
}

// пересчет
fun currentValue(vars: SnapshotStateList<Variable>,
                 state: CodeBlockState,
            block: ForBlock,
            context: Context,
            arrays: List<ArrayBlock>): Double {
    val expr = vars.first {it.name == block.variable}.expression
    val rpn = convertToReversePolishNotation(expr, context)
    return calculateArithmeticExpression(
        expression = rpn,
        state = state,
        context = context,
        arrays = arrays,
    )
}

// это для записи пересчитанного значения переменной после каждой итерации
fun writeCurrValue(v: Double,
          vars: SnapshotStateList<Variable>,
          index: Int) {
    val s = if (vars[index].type == VariableType.INT) v.toInt().toString() else v.toString()
    vars[index] = vars[index].copy(expression = s)
}

//приоритеты операций
fun getPriority(operator: Char): Int = when(operator){
    '+','-' -> 1
    '*','/', '%' -> 2
    else -> 0
}

// Предварительно обрабатываем выражение с массивами
fun preprocessArrayExpression(expression: String) : String {
    val startsWithArray = expression.trim().matches(Regex("^[a-zA-Z_]\\w*\\s*\\[.*"))
    val modifiedExpr = if (startsWithArray) "1*${expression}" else expression

    val arrPattern = Regex("([a-zA-Z_]\\w*)\\s*\\[(.*?)\\]")

    var res = modifiedExpr
    var offset = 0
    arrPattern.findAll(modifiedExpr).forEach { matchRes ->
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

// это для UI, для красоты
fun preprocessArrayExprForDisplay(expression: String) : String {
    var cleanedExpr = if (expression.startsWith("1*"))
        expression.substring(2)
    else expression
    val arrPattern = Regex("([a-zA-Z_]\\w*)\\s*\\[(.*?)\\]")

    cleanedExpr = cleanDisplayExpr(cleanedExpr)
    var res = cleanedExpr
    var offset = 0
    arrPattern.findAll(cleanedExpr).forEach { matchRes ->
        val arrName = matchRes.groupValues[1]
        val idExpr = matchRes.groupValues[2]

        val arrayToken = "${arrName}[${idExpr}]"
        val startPos = matchRes.range.first + offset
        val endPos = matchRes.range.last + offset + 1
        res = res.substring(0, startPos) + arrayToken + res.substring(endPos)
        offset += arrayToken.length - (endPos - startPos)
    }
    return res
}

fun cleanDisplayExpr(expression: String) : String {
    var cleaned = expression
    cleaned = cleaned
        .replace(Regex("^0-"), "-")
        .replace(Regex("\\(0-"), "(-")
        .replace(Regex("([+\\-*/]\\s*)0-"), "$1-")
    return cleaned
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
        return ""
    }

    while (i < processedExpr.length) {
        val c = processedExpr[i]

        when {
            c.isDigit() || c == '.' -> {
                while (i < processedExpr.length && (processedExpr[i].isDigit() || processedExpr[i] == '.')){
                    output.append(processedExpr[i++])
                }
                output.append(' ')
                continue
            }

            c.isLetter() || c == '_' ->{
                while (i <processedExpr.length && (processedExpr[i].isLetterOrDigit() || processedExpr[i] == '_')){
                    output.append(processedExpr[i++])
                }
                if (i < processedExpr.length && processedExpr[i] == '[') {
                    output.append('[')
                    i++

                    var bracketLevel = 1
                    while (i < processedExpr.length && bracketLevel > 0) {
                        when (processedExpr[i]) {
                            '[' -> bracketLevel++
                            ']' -> bracketLevel--
                        }
                        output.append(processedExpr[i++])
                    }
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
                    return ""
                }
                stack.pop()
                i++
            }

            c in "+-*/%" ->{
                while (stack.isNotEmpty() && stack.peek() != '(' &&
                    getPriority(stack.peek()) >= getPriority(c)) {
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
    state: CodeBlockState,
    context: Context
): Double {
    try {
        val mas = arrays.firstOrNull { it.name == arrName }
        if (mas == null) {
            Toast.makeText(
                context,
                context.getString(R.string.err_array_not_found, arrName),
                Toast.LENGTH_LONG
            ).show()
            return 0.0
        }

        val idRpn = convertToReversePolishNotation(indexExpression, context)
        val iVal = calculateArithmeticExpression(idRpn, state, context = context, arrays = arrays)
        if (iVal != iVal.toInt().toDouble()) {
            Toast.makeText(
                context,
                context.getString(R.string.err_array_index_must_be_integer, iVal.toString()),
                Toast.LENGTH_LONG
            ).show()
            return 0.0
        }
        val i = iVal.toInt()
        if (i < 0 || i >= mas.size) {
            Toast.makeText(
                context,
                context.getString(R.string.err_array_index_out_of_bounds, i.toString()),
                Toast.LENGTH_LONG
            ).show()
            return 0.0
        }
        return mas.elems.getOrNull(i)?.toDoubleOrNull() ?: 0.0
    }
    catch (e: Exception) {
        Toast.makeText(
            context,
            context.getString(R.string.err_accessing_array_element, e.message),
            Toast.LENGTH_LONG
        ).show()
        return 0.0
    }
}

fun setArrayElement(
    arrName: String,
    indexExpression: String,
    valueExpression: String,
    arrays: SnapshotStateList<ArrayBlock>,
    state: CodeBlockState,
    context: Context
): Boolean {
    try {
        Log.d("SetArray", "Setting $arrName[$indexExpression] = $valueExpression")
        val mas = arrays.find { it.name == arrName }
        if (mas == null) {
            Log.e("SetArray", "Array $arrName not found")
            Toast.makeText(
                context,
                context.getString(R.string.err_array_not_found, arrName),
                Toast.LENGTH_LONG
            ).show()
            return false
        }
        val idRpn = convertToReversePolishNotation(indexExpression, context)
        val iVal = calculateArithmeticExpression(idRpn, state, context = context, arrays = arrays)
        if (iVal != iVal.toInt().toDouble()) {
            Log.e("SetArray", "Index $iVal is not integer")
            Toast.makeText(
                context,
                context.getString(R.string.err_array_index_must_be_integer, iVal.toString()),
                Toast.LENGTH_LONG
            ).show()
            return false
        }

        val i = iVal.toInt()
        if (i < 0 || i >= mas.size) {
            Log.e("SetArray", "Index $i out of bounds for array size ${mas.size}")
            Toast.makeText(
                context,
                context.getString(R.string.err_array_index_out_of_range, i.toString(), mas.name, mas.size),
                Toast.LENGTH_LONG
            ).show()
            return false
        }
        val valueRpn = convertToReversePolishNotation(valueExpression, context)
        val value = calculateArithmeticExpression(valueRpn, state, context = context, arrays = arrays)

        val id = arrays.indexOf(mas)
        if (id >= 0) {
            val newElems = mas.elems.toMutableList()
            val oldValue = newElems[i]
            newElems[i] = formatNumber(value)
            arrays[id] = mas.copy(elems = newElems)

            Log.d("SetArray", "Successfully set $arrName[$i] from $oldValue to ${formatNumber(value)}")
            Log.d("SetArray", "Array now: ${newElems}")
            return true
        }
        else {
            Log.e("SetArray", "Failed to find array in list")
            return false
        }
    }
    catch (e: Exception) {
        Log.e("SetArray", "Exception: ${e.message}", e)
        Toast.makeText(
            context,
            context.getString(R.string.err_setting_array_element, e.message),
            Toast.LENGTH_LONG
        ).show()
        return false
    }
}

// вычисляем значение арифметического выражения
fun calculateArithmeticExpression(
    expression: String,
    state: CodeBlockState,
    context: Context,
    arrays: List<ArrayBlock> = emptyList()
): Double {
    Log.d("CalcExpr", "Evaluating expression: $expression")
    if (state.vars.any {it.name.isEmpty()}){
        Toast.makeText(context, R.string.err_var_with_enpty_name_found, Toast.LENGTH_LONG).show()
    }

    val startsWithArray = expression.trim().matches(Regex("^[a-zA-Z]\\w*\\s*\\[.*"))
    val processedExpr = if (startsWithArray) "1 ${expression}" else expression

    val arrayAccessPattern = Regex("([a-zA-Z_]\\w*)\\[(.*)\\]")

    val isAlreadyRpn = processedExpr.trim().split(" ").all {
        it.toDoubleOrNull() != null || it in listOf("+", "-", "*", "/", "%") || state.vars.any { v -> v.name == it } || Regex("[a-zA-Z_]\\w*\\[.*\\]").matches(it)
    }

    val rpnExpr = if (!isAlreadyRpn) {
        convertToReversePolishNotation(expression, context)
    } else {
        expression
    }

    val tokens = rpnExpr.split(" ").filter { it.isNotBlank() }
    val stack = mutableListOf<Double>()
    Log.d("CalcExpr", "Tokens: $tokens")

    if (tokens.isEmpty()){
        Log.e("CalcExpr", "Empty expression")
        Toast.makeText(context, R.string.err_empty_exp, Toast.LENGTH_LONG).show()
        return 0.0
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
                        val iVal = calculateArithmeticExpression(
                            idRpn,
                            state,
                            context = context,
                            arrays = arrays
                        )
                        var i0 = iVal.toInt()
                        if (iVal != i0.toDouble()) {
                            Toast.makeText(
                                context,
                                context.getString(R.string.err_array_index_must_be_integer, iVal.toString()),
                                Toast.LENGTH_LONG
                            ).show()
                            stack.add(0.0)
                            continue
                        }
                        var i = calculateArithmeticExpression(idRpn, state, context = context, arrays = arrays).toInt()
                        if (i >= 0 && i < mas.size) {
                            val value = mas.elems.getOrNull(i)?.toDoubleOrNull() ?: 0
                            stack.add(value.toDouble())
                        }
                        else {
                            Toast.makeText(
                                context,
                                context.getString(R.string.err_array_index_out_of_bounds, i.toString()),
                                Toast.LENGTH_LONG
                            ).show()
                            stack.add(0.0)
                        }
                    }
                    else {
                        Log.e("CalcExpr", "Array not found: $arrName")
                        Toast.makeText(
                            context,
                            context.getString(R.string.err_array_not_found, arrName),
                            Toast.LENGTH_LONG
                        ).show()
                        stack.add(0.0)
                    }
                }

                token.toDoubleOrNull() != null -> {
                    Log.d("CalcExpr", "Numeric token: $token")
                    stack.add(token.toDouble())
                }

                state.vars.any { it.name == token } -> {
                    val variable = state.vars.first {it.name == token}
                    Log.d("CalcExpr", "Variable token: ${variable.name} = ${variable.expression}")
                    if (variable.expression.contains(Regex("\\b${variable.name}\\b"))){
                        stack.add(variable.value.toString().toDouble())
                    }
                    else {
                        val value = if (variable.expression.matches(Regex("-?\\d+(\\.\\d+)?")))
                            variable.expression.toDouble()
                        else if (variable.expression.contains(Regex("\\b${variable.name}\\b")))
                            variable.value.toString().toDouble()
                        else {
                            val rpn = convertToReversePolishNotation(variable.expression, context)
                            Log.d("CalcExpr", "Variable expression RPN: $rpn")
                            calculateArithmeticExpression(
                                rpn,
                                state,
                                context = context,
                                arrays = arrays
                            )
                        }
                        Log.d("CalcExpr", "Variable $token,  value: $value")
                        stack.add(value)
                    }
                }

                token == "+" -> {
                    if (stack.size < 2) {
                        Toast.makeText(
                            context,
                            context.getString(R.string.err_not_enough_operands, "+"),
                            Toast.LENGTH_LONG
                        ).show()
                        return 0.0
                    }
                    val b = stack.removeAt(stack.lastIndex)
                    val a = stack.removeAt(stack.lastIndex)
                    stack.add(a + b)
                }

                token == "-" -> {
                    if (stack.size < 2) {
                        Toast.makeText(
                            context,
                            context.getString(R.string.err_not_enough_operands, "-"),
                            Toast.LENGTH_LONG
                        ).show()
                        return 0.0
                    }
                    val b = stack.removeAt(stack.lastIndex)
                    val a = stack.removeAt(stack.lastIndex)
                    stack.add(a - b)
                }

                token == "*" -> {
                    if (stack.size < 2) {
                        Toast.makeText(
                            context,
                            context.getString(R.string.err_not_enough_operands, "*"),
                            Toast.LENGTH_LONG
                        ).show()
                        return 0.0
                    }
                    val b = stack.removeAt(stack.lastIndex)
                    val a = stack.removeAt(stack.lastIndex)
                    stack.add(a * b)
                }

                token == "/" -> {
                    if (stack.size < 2) {
                        Toast.makeText(
                            context,
                            context.getString(R.string.err_not_enough_operands, "/"),
                            Toast.LENGTH_LONG
                        ).show()
                        return 0.0
                    }
                    val b = stack.removeAt(stack.lastIndex)
                    if (b == 0.0) {
                        Log.e("CalcExpr", "Division by zero")
                        Toast.makeText(context, R.string.err_div_by_zero, Toast.LENGTH_LONG).show()
                    }
                    val a = stack.removeAt(stack.lastIndex)
                    stack.add(a / b)
                }

                token == "%" -> {
                    if (stack.size < 2) {
                        Toast.makeText(
                            context,
                            context.getString(R.string.err_not_enough_operands, "%"),
                            Toast.LENGTH_LONG
                        ).show()
                        return 0.0
                    }
                    val b = stack.removeAt(stack.lastIndex)
                    if (b == 0.0) {
                        Log.e("CalcExpr", "Modulo by zero")
                        Toast.makeText(context, R.string.err_div_by_zero, Toast.LENGTH_LONG).show()
                    }
                    val a = stack.removeAt(stack.lastIndex)
                    stack.add(a % b)
                }
            }
        }
        catch (e: NoSuchElementException){
            Toast.makeText(context, context.getString(R.string.err_var_token_not_found, token), Toast.LENGTH_LONG).show()
        }
        catch (e: Exception) {
            Toast.makeText(
                context,
                context.getString(R.string.error_detailed, e.message),
                Toast.LENGTH_LONG
            ).show()
            return 0.0
        }
    }

    val result = stack.singleOrNull()
    if (result == null){
        Toast.makeText(context, R.string.err_invalid_exp_format, Toast.LENGTH_LONG).show()
        return 0.0
    }
    return result
}

//находим зависимость переменной от других переменных
fun extractDependencies(expression: String): Set<String>{
    val varPattern = Regex("[a-zA-Z_]\\w*")
    val arrPattern = Regex("([a-zA-Z_]\\w*)\\s*\\[")

    val vars = varPattern.findAll(expression)
        .map { it.value }
        .toSet()
    val arrVars = arrPattern.findAll(expression)
        .map { it.groupValues[1] }
        .toSet()
    return vars + arrVars
}

// здесь мы пересчитываем все переменные
fun recalculateAllVariables(
    state: CodeBlockState,
    context: Context,
    arrays: List<ArrayBlock> = emptyList()
) : Result<List<Variable>> {
    if (state.vars.isEmpty()) return Result.success(emptyList())

    val graph = mutableMapOf<String, Set<String>>()
    for (variable in state.vars) {
        graph[variable.name] = extractDependencies(variable.expression)
    }
    for (array in arrays) {
        graph[array.name] = emptySet()
    }

    return runCatching {
        val updatedVars = state.vars.map { it.copy() }.toMutableList()
        val sortedOrder = topologicalSort(graph)
        val computedValues = mutableMapOf<String, Double>()

        val arrayNames = arrays.map { it.name }.toSet()
        for (varName in sortedOrder) {
            if (arrayNames.contains(varName)) continue

            val variable = updatedVars.firstOrNull { it.name == varName }
            if (variable == null) {
                Log.e("RecalculateVars", "Variable $varName not found in collection")
                Toast.makeText(
                    context,
                    context.getString(R.string.err_var_token_not_found, varName),
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
                        state,
                        context
                    )
                    if (checkTypeCompatibility(variable, value, context)) {
                        computedValues[varName] = value
                        updatedVars.replaceAll {
                            if (it.name == varName) {
                                val formatted = if (it.type == VariableType.INT)
                                    value.toInt().toString()
                                else formatNumber(value)
                                it.copy(value = formatted)
                            } else it
                        }
                    }
                }
                else {
                    Log.d("RecalculateVars", "Variable ${variable.name} original expr: ${variable.value}")
                    var processed = variable.expression
                    computedValues.forEach { (name, oldValue) ->
                        processed = processed.replace(name, oldValue.toString())
                    }
                    Log.d("RecalculateVars", "Variable ${variable.name} processed expr: $processed")
                    val rpn = convertToReversePolishNotation(processed, context)
                    val value = calculateArithmeticExpression(
                        rpn,
                        state,
                        context = context,
                        arrays = arrays
                    )
                    computedValues[varName] = value
                    updatedVars.replaceAll {
                        if (it.name == varName) {
                            val formatted = if (it.type == VariableType.INT)
                                value.toInt().toString()
                            else formatNumber(value)
                            it.copy(value = formatted)
                        }
                        else it
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
fun dfs(
    graph: Map<String, Set<String>>,
    node: String,
    visited: MutableMap<String, Int>,
    order: MutableList<String>
){
    when (visited[node]){
        1 -> {
            if (graph[node]?.contains(node) == true){
                visited[node] = 2
                order.add(node)
                return
            }
            throw IllegalArgumentException("Cyclic dependency")
        }
        2 -> return
    }

    visited[node] = 1

    graph[node]?.forEach { neighbor ->
        if (neighbor != node) {
            dfs(graph, neighbor, visited, order)
        }
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
    val result = recalculateAllVariables(state, context, state.arrays)
    result.onSuccess { updated ->
        state.vars.clear()
        state.vars.addAll(updated)
        // Также с условиями!
        state.ifBlock.forEach { block ->
            val conditionRes = evaluateIfCondition(
                block.leftExpression,
                block.rightExpression,
                block.comparisonOperator,
                state,
                context,
                state.arrays
            )
            if (conditionRes) {
                executeCommands(block.commands, state, context, state.arrays)
            } else {
                executeCommands(block.elseCommands, state, context, state.arrays)
            }
        }
        // и с циклом while
        state.whileBlocks.forEach {
            block -> executeCommands(
                listOf(WhileBlockCommand(block)),
                state,
                context,
                state.arrays
            )
        }

        state.forBlocks.forEach { block ->
            executeCommands(
                listOf(ForBlockCommand(block)),
                state,
                context,
                state.arrays
            )
        }
    }.
    onFailure { e ->
        Log.e("RecCalAll", "Error recalculating variables: ${e.message}", e)
        Toast.makeText(
            context,
            e.message ?: context.getString(R.string.error),
            Toast.LENGTH_LONG
        ).show()
    }
}

// проверка на валидность арифм операций со скобками
fun isValidArithmExpression(state: CodeBlockState) : Boolean {
    val processedExpr = preprocessArrayExprForDisplay(state.assignmentArithmExpr)
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

    val regex = Regex("[A-Za-z_]\\w*(?:\\[(?:[^\\[\\]]+)\\])?|\\d+(?:\\.\\d+)?|[()+\\-*/%\\[\\]]")
    val tokens = regex.findAll(state.assignmentArithmExpr).map { it.value }.toList()
    if (tokens.isEmpty()) return false

    // машина состояний которая ждет после числа/переменной оператор и наоборот
    var expect = true
    for (t in tokens) {
        if (expect) {
            when {
                t.matches(Regex("\\d+")) ||
                        t.matches(Regex("-?\\d+(?:\\.\\d+)?")) ||
                        t.matches(Regex("(?!_|\\d+)([a-zA-Z_]\\w*)")) ||
                        t.matches(Regex("(?!_|\\d+)([a-zA-Z_]\\w*)\\[.*\\]"))
                    -> expect = false

                t == "-" && (tokens.indexOf(t) == 0 ||
                        tokens.getOrNull(tokens.indexOf(t) - 1) in listOf("(", "["))
                            -> expect = true
                t == "(" -> expect = true
                t == "[" -> expect = true
                t == "+" || t == "-" -> expect = true
                else -> return false
            }
        }
        else {
            expect = when (t) {
                "+", "-", "/", "%", "*" -> true
                ")" -> false
                "]" -> false
                else -> return false
            }
        }
    }
    return !expect
}

// перезаписываем такую запись как например 2(1+3) в 2*(1+3)
// Или унарный минус
fun rewriteExpression(expression: String) : String {
    var str = expression
    str = str
        .replace(Regex("^\\s*-"), "0-")
        .replace(Regex("\\(\\s*-"), "(0-")

    val minusReplacements = mutableListOf<Pair<IntRange, String>>()
    Regex("([+\\-*/]\\s*)-\\s*(\\d+(?:\\.\\d+)?|[a-zA-Z_]\\w*|\\()").findAll(str).forEach { m ->
        val operator = m.groupValues[1]
        var operand = m.groupValues[2]

        val replacement = if (operand == "(")
            "${operator}(0-("
        else "${operator}(0-${operand})"
        minusReplacements.add(m.range to replacement)
    }
    minusReplacements.reversed().forEach { (range, repl) ->
        str = str.substring(0, range.first) + repl + str.substring(range.last + 1)
    }

    str = Regex("([A-Za-z_]\\w*|\\d+)\\s*\\(").replace(str) { m ->
        "${m.groupValues[1]}*("
    }

    str = Regex("(\\d+(?:\\.\\d+)?)([A-Za-z_]\\w*)").replace(str) { m ->
        "${m.groupValues[1]}*${m.groupValues[2]}"
    }
    str = preprocessArrayExpression(str)
    return str
}
