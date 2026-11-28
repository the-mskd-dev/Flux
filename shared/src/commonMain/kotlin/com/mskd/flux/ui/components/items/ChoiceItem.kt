package com.mskd.flux.ui.components.items

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import com.mskd.flux.ui.Design
import com.mskd.flux.ui.components.containers.ExpressiveRow
import com.mskd.flux.ui.motion.Motion
import com.mskd.flux.ui.motion.expressiveBounceOnChange
import com.mskd.flux.ui.text.Text

internal enum class ChoiceType {
    Checkbox,
    Radio,
}

@Composable
internal fun ChoiceItem(
    text: String,
    selected: Boolean,
    type: ChoiceType,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = Color.Transparent,
    textStyle: TextStyle = Text.Style.listBody(),
    indicator: @Composable (Modifier) -> Unit,
) {
    val selectionModifier = when (type) {
        ChoiceType.Checkbox -> Modifier.toggleable(
            value = selected,
            role = Role.Checkbox,
            onValueChange = { onClick() },
        )
        ChoiceType.Radio -> Modifier.selectable(
            selected = selected,
            role = Role.RadioButton,
            onClick = onClick,
        )
    }

    ExpressiveRow(
        modifier = modifier
            .fillMaxWidth()
            .clip(CircleShape)
            .then(selectionModifier)
            .background(containerColor)
            .padding(Design.Space.medium),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Design.Space.small),
    ) {
        val content = createRef()

        indicator(
            Modifier
                .expressiveBounceOnChange(
                    state = selected,
                    enabled = selected,
                    animationSpec = Motion.press.toSpec(),
                )
                .expressiveSideBounceOnChange(
                    state = selected,
                    enabled = selected,
                    right = content,
                )
        )

        Text.Adaptive(
            modifier = Modifier
                .expressiveSideBounce(ref = content)
                .weight(1f),
            text = text,
            style = textStyle,
        )
    }
}
