package com.example.presaber.ui.institution.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.presaber.R

@Composable
fun CreateCourseHeader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.img_course_new),
            contentDescription = "Nuevo curso",
            modifier = Modifier
                .width(280.dp)
                .height(130.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CreateCourseHeaderPreview() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CreateCourseHeader()
    }
}

