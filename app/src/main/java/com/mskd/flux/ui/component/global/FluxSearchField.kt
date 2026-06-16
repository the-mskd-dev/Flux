package com.mskd.flux.ui.component.global

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mskd.flux.R
import com.mskd.flux.screens.search.SearchIntent

@Composable
fun FluxSearchField(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
) {

    TextField(
        modifier = modifier,
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        shape = MaterialTheme.shapes.extraLargeIncreased,
        colors = TextFieldDefaults.colors(
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent
        ),
        placeholder = { Text(stringResource(R.string.enter_search)) },
        trailingIcon = {
            if (value.isNotEmpty()) {
                IconButton(
                    modifier = Modifier.size(18.dp),
                    onClick = { onValueChange("") },
                    content = { Icon(imageVector = Icons.Rounded.Clear, contentDescription = "clear button") }
                )
            }
        }
    )

}