package com.example.presaber.ui.institution.components.teachers

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

@Composable
fun TeacherItem(
    teacher: Teacher,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(4.dp)
    ) {
        if (teacher.photoUrl != null) {
            AsyncImage(
                model = teacher.photoUrl,
                contentDescription = teacher.name,
                modifier = Modifier
                    .size(48.dp)
                    .clip(MaterialTheme.shapes.large)
            )
        } else {
            Image(
                painter = painterResource(id = teacher.imageRes),
                contentDescription = teacher.name,
                modifier = Modifier
                    .size(48.dp)
                    .clip(MaterialTheme.shapes.large)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))
        Text(text = teacher.name, color = MaterialTheme.colorScheme.onSurface)
    }
}
