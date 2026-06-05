package com.jrr.jrrkmp_native_ui.tv.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import com.jrr.jrrkmp_native_ui.tv.ui.theme.JrrGold
import com.jrr.jrrkmp_native_ui.tv.ui.theme.JrrMuted
import com.jrr.jrrkmp_native_ui.tv.ui.theme.JrrOnSurface

/**
 * D-pad friendly single-line text field. Focusing it (and pressing the center
 * button) raises the system IME; the border turns gold while focused so the
 * remote user can see where they are.
 */
@Composable
fun TvTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    isPassword: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Default,
    onImeAction: (() -> Unit)? = null,
) {
    var focused by remember { mutableStateOf(false) }
    Column(modifier = modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Text(text = label, color = JrrMuted, modifier = Modifier.padding(bottom = 4.dp))
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = TextStyle(color = JrrOnSurface, fontSize = 20.sp),
            cursorBrush = SolidColor(JrrGold),
            visualTransformation =
                if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
            keyboardActions = KeyboardActions(
                onDone = { onImeAction?.invoke() },
                onSearch = { onImeAction?.invoke() },
                onGo = { onImeAction?.invoke() },
            ),
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { focused = it.isFocused }
                .border(
                    width = 2.dp,
                    color = if (focused) JrrGold else JrrMuted,
                    shape = RoundedCornerShape(8.dp),
                )
                .padding(horizontal = 16.dp, vertical = 14.dp),
        )
    }
}
