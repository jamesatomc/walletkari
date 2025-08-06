package network.kanari.wallet_kari.components.widget

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuDefaults.textFieldColors
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import network.kanari.wallet_kari.R


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onPasswordVisibilityChange: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    shadowElevation: Dp = 8.dp,
    visualTransformation: PasswordVisualTransformation,
    isError: Boolean,
    placeholderColor: Color = Color.Gray,
    labelColor: Color = Color.Black,
    borderColor: Color = Color.Transparent,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default // Set default keyboard options
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = labelColor) },
        placeholder = { Text(placeholder, color = placeholderColor) },
        visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = keyboardOptions, // Use keyboardOptions parameter
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .border(
                BorderStroke(
                    color = borderColor,
                    width = 1.dp
                ),
                shape = RoundedCornerShape(10.dp)
            )
            .clip(RoundedCornerShape(10.dp))
            .shadow(shadowElevation, RoundedCornerShape(10.dp)),
        colors = textFieldColors(
            unfocusedIndicatorColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
        ),
        trailingIcon = if (isPassword) {
            {
                val image = if (passwordVisible) {
                    painterResource(id = R.drawable.visibility)
                } else {
                    painterResource(id = R.drawable.visibility_off)
                }

                IconButton(onClick = onPasswordVisibilityChange!!) {
                    Icon(
                        modifier = Modifier.size(24.dp),
                        painter = image,
                        contentDescription = if (passwordVisible) "Hide password" else "Show password"
                    )
                }
            }
        } else null
    )
}