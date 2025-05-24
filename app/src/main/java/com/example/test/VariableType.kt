package com.example.test

enum class VariableType {
    INT, FLOAT;

    override fun toString(): String = when(this) {
        INT -> "Int"
        FLOAT -> "Float"
    }
}