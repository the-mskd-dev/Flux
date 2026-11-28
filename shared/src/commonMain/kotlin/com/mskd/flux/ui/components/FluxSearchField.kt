package com.mskd.flux.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mskd.flux.ui.motion.Motion
import flux.shared.generated.resources.Res
import flux.shared.generated.resources.enter_search
import org.jetbrains.compose.resources.stringResource

@Composable
fun FluxSearchField(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
) {

    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val cornerSize by animateDpAsState(
        targetValue = if (isFocused) 32.dp else 16.dp,
        animationSpec = Motion.contentSize.toSpec(),
        label = "TextFieldShapeAnimation"
    )

    TextField(
        modifier = modifier,
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        shape = RoundedCornerShape(cornerSize),
        colors = TextFieldDefaults.colors(
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent
        ),
        placeholder = { Text(stringResource(Res.string.enter_search)) },
        trailingIcon = {
            if (value.isNotEmpty()) {
                IconButton(
                    modifier = Modifier.size(18.dp),
                    onClick = { onValueChange("") },
                    content = { Icon(imageVector = Icons.Rounded.Clear, contentDescription = "clear button") }
                )
            }
        },
        interactionSource = interactionSource
    )

}