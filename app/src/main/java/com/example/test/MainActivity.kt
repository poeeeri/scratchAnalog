package com.example.test

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Preview
import com.example.test.ui.theme.CodeBlock
import com.example.test.ui.theme.TestTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TestTheme {
                CodeBlock()
            }
        }
    }
}

// ребята как же андроид студио тормозит епрст
// (」＞＜)」
// как же андроид студио жрёт заряд батареи...
// как же адроид студио...

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    TestTheme {
        CodeBlock()
    }
}