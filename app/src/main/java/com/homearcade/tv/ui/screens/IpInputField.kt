package com.homearcade.tv.ui.screens

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun IpInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    modifier: Modifier = Modifier,
    isNumeric: Boolean = false
) {
    val focusManager = LocalFocusManager.current

    val colors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = androidx.compose.ui.graphics.Color(0xFFEC4899),
        unfocusedBorderColor = androidx.compose.ui.graphics.Color(0x33F8FAFC),
        cursorColor = androidx.compose.ui.graphics.Color(0xFFEC4899),
        focusedLabelColor = androidx.compose.ui.graphics.Color(0xFFEC4899),
        unfocusedLabelColor = androidx.compose.ui.graphics.Color(0x99F8FAFC),
        focusedTextColor = androidx.compose.ui.graphics.Color(0xFFF8FAFC),
        unfocusedTextColor = androidx.compose.ui.graphics.Color(0xFFF8FAFC)
    )

    OutlinedTextField(
        value = value,
        onValueChange = { v ->
            if (isNumeric) {
                onValueChange(v.filter { c -> c.isDigit() || c.isLetter() })
            } else {
                onValueChange(v)
            }
        },
        label = { androidx.compose.material3.Text(label, fontSize = 14.sp) },
        placeholder = { androidx.compose.material3.Text(placeholder, fontSize = 14.sp) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = if (isNumeric) KeyboardType.Number else KeyboardType.Uri,
            imeAction = ImeAction.Next
        ),
        keyboardActions = KeyboardActions(
            onNext = { focusManager.moveFocus(FocusDirection.Down) },
            onDone = { focusManager.clearFocus() }
        ),
        colors = colors,
        modifier = modifier.fillMaxWidth(),
        textStyle = androidx.compose.material3.MaterialTheme.typography.bodyLarge.copy(fontSize = 20.sp)
    )
}
