package tech.dojo.pay.uisdk.presentation.ui.virtualterminalcheckout

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import tech.dojo.pay.uisdk.R
import tech.dojo.pay.uisdk.presentation.components.CheckBoxItem
import tech.dojo.pay.uisdk.presentation.components.CountrySelectorField
import tech.dojo.pay.uisdk.presentation.components.DescriptionField
import tech.dojo.pay.uisdk.presentation.components.InputFieldWithErrorMessage
import tech.dojo.pay.uisdk.presentation.components.theme.DojoTheme
import tech.dojo.pay.uisdk.presentation.components.theme.medium
import tech.dojo.pay.uisdk.presentation.ui.virtualterminalcheckout.state.ShippingAddressViewState
import tech.dojo.pay.uisdk.presentation.ui.virtualterminalcheckout.viewmodel.VirtualTerminalViewModel
import kotlin.math.roundToInt

@OptIn(ExperimentalComposeUiApi::class)
@Composable
internal fun ShippingAddressSection(
    viewModel: VirtualTerminalViewModel,
    coroutineScope: CoroutineScope,
    scrollToPosition: Float,
    scrollState: ScrollState,
    keyboardController: SoftwareKeyboardController?
) {
    val state = viewModel.state.observeAsState().value ?: return
    if (state.shippingAddressSection?.isVisible == true) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.background(DojoTheme.colors.primarySurfaceBackgroundColor)
        ) {
            HeaderTitle()
            NameField(
                state.shippingAddressSection,
                viewModel,
                coroutineScope,
                scrollToPosition,
                scrollState,
                keyboardController
            )
            Address1Field(
                state.shippingAddressSection,
                viewModel,
                coroutineScope,
                scrollToPosition,
                scrollState,
                keyboardController
            )
            Address2Field(
                state.shippingAddressSection,
                viewModel,
                coroutineScope,
                scrollToPosition,
                scrollState,
                keyboardController
            )
            CityField(
                state.shippingAddressSection,
                viewModel,
                coroutineScope,
                scrollToPosition,
                scrollState,
                keyboardController
            )
            PostalCodeField(
                state.shippingAddressSection,
                viewModel,
                coroutineScope,
                scrollToPosition,
                scrollState,
                keyboardController
            )
            CountryField(
                state.shippingAddressSection,
                viewModel
            )
            DeliveryNotesField(
                state.shippingAddressSection,
                viewModel,
                coroutineScope,
                scrollToPosition,
                scrollState
            )
            AddressesAreTheSameCheckBox(
                state.shippingAddressSection,
                viewModel
            )
        }
    }
}

@Composable
private fun HeaderTitle() {
    Text(
        text = stringResource(id = R.string.dojo_ui_sdk_card_details_checkout_title_shipping),
        overflow = TextOverflow.Ellipsis,
        maxLines = 1,
        style = DojoTheme.typography.h6.medium,
        color = DojoTheme.colors.primaryLabelTextColor.copy(alpha = ContentAlpha.high)
    )
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun NameField(
    shippingAddressSection: ShippingAddressViewState,
    viewModel: VirtualTerminalViewModel,
    coroutineScope: CoroutineScope,
    scrollToPosition: Float,
    scrollState: ScrollState,
    keyboardController: SoftwareKeyboardController?
) {
    var isTextNotFocused by remember { mutableStateOf(false) }
    val scrollOffset = with(LocalDensity.current) {
        shippingAddressSection.itemPoissonOffset.dp.toPx() + NORMAL_FILED_SIZE_DP.dp.toPx()
    }
    InputFieldWithErrorMessage(
        modifier = Modifier.onFocusChanged {
            isTextNotFocused = if (it.isFocused) {
                coroutineScope.launch {
                    delay(300)
                    scrollState.animateScrollTo(
                        scrollToPosition.roundToInt() + scrollOffset.roundToInt()
                    )
                }
                true
            } else {
                if (isTextNotFocused) {
                    viewModel.onValidateShippingNameField(shippingAddressSection.name.value)
                }
                false
            }
        },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { keyboardController?.hide() }),
        value = shippingAddressSection.name.value,
        isError = shippingAddressSection.name.isError,
        assistiveText = shippingAddressSection.name.errorMessages?.let {
            AnnotatedString(stringResource(id = it))
        },
        onValueChange = { viewModel.onNameFieldChanged(it) },
        label = buildAnnotatedString { append(stringResource(id = R.string.dojo_ui_sdk_card_details_checkout_field_shipping_name)) }
    )
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun Address1Field(
    shippingAddressSection: ShippingAddressViewState,
    viewModel: VirtualTerminalViewModel,
    coroutineScope: CoroutineScope,
    scrollToPosition: Float,
    scrollState: ScrollState,
    keyboardController: SoftwareKeyboardController?
) {
    var isTextNotFocused by remember { mutableStateOf(false) }
    val scrollOffset = with(LocalDensity.current) {
        shippingAddressSection.itemPoissonOffset.dp.toPx() + (2 * NORMAL_FILED_SIZE_DP).dp.toPx()
    }
    InputFieldWithErrorMessage(
        modifier = Modifier
            .onFocusChanged {
                isTextNotFocused = if (it.isFocused) {
                    coroutineScope.launch {
                        delay(300)
                        scrollState.animateScrollTo(
                            scrollToPosition.roundToInt() + scrollOffset.roundToInt()
                        )
                    }
                    true
                } else {
                    if (isTextNotFocused) {
                        viewModel.onValidateAddress1Field(
                            shippingAddressSection.addressLine1.value, true
                        )
                    }
                    false
                }
            }
            .padding(top = 16.dp),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { keyboardController?.hide() }),
        value = shippingAddressSection.addressLine1.value,
        isError = shippingAddressSection.addressLine1.isError,
        assistiveText = shippingAddressSection.addressLine1.errorMessages?.let {
            AnnotatedString(stringResource(id = it))
        },
        onValueChange = { viewModel.onAddress1FieldChanged(it, true) },
        label = buildAnnotatedString { append(stringResource(id = R.string.dojo_ui_sdk_card_details_checkout_field_shipping_line_1)) }
    )
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun Address2Field(
    shippingAddressSection: ShippingAddressViewState,
    viewModel: VirtualTerminalViewModel,
    coroutineScope: CoroutineScope,
    scrollToPosition: Float,
    scrollState: ScrollState,
    keyboardController: SoftwareKeyboardController?
) {
    val scrollOffset = with(LocalDensity.current) {
        shippingAddressSection.itemPoissonOffset.dp.toPx() + (3 * NORMAL_FILED_SIZE_DP).dp.toPx()
    }
    val label = buildAnnotatedString {
        append(stringResource(id = R.string.dojo_ui_sdk_card_details_checkout_field_shipping_line_2))
        append(" ")
        withStyle(SpanStyle(DojoTheme.colors.primaryLabelTextColor.copy(alpha = ContentAlpha.medium))) { append(stringResource(id = R.string.dojo_ui_sdk_dojo_ui_sdk_card_details_checkout_optional)) }
    }
    InputFieldWithErrorMessage(
        modifier = Modifier
            .padding(top = 16.dp)
            .onFocusChanged {
                if (it.isFocused) {
                    coroutineScope.launch {
                        delay(300)
                        scrollState.animateScrollTo(
                            scrollToPosition.roundToInt() + scrollOffset.roundToInt()
                        )
                    }
                }
            },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { keyboardController?.hide() }),
        value = shippingAddressSection.addressLine2.value,
        isError = shippingAddressSection.addressLine2.isError,
        assistiveText = shippingAddressSection.addressLine2.errorMessages?.let {
            AnnotatedString(stringResource(id = it))
        },
        onValueChange = { viewModel.onAddress2FieldChanged(it, true) },
        label = label
    )
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun CityField(
    shippingAddressSection: ShippingAddressViewState,
    viewModel: VirtualTerminalViewModel,
    coroutineScope: CoroutineScope,
    scrollToPosition: Float,
    scrollState: ScrollState,
    keyboardController: SoftwareKeyboardController?
) {
    var isTextNotFocused by remember { mutableStateOf(false) }
    val scrollOffset = with(LocalDensity.current) {
        shippingAddressSection.itemPoissonOffset.dp.toPx() + (4 * NORMAL_FILED_SIZE_DP).dp.toPx()
    }
    InputFieldWithErrorMessage(
        modifier = Modifier
            .onFocusChanged {
                isTextNotFocused = if (it.isFocused) {
                    coroutineScope.launch {
                        delay(300)
                        scrollState.animateScrollTo(
                            scrollToPosition.roundToInt() + scrollOffset.roundToInt()
                        )
                    }
                    true
                } else {
                    if (isTextNotFocused) {
                        viewModel.onValidateCityField(shippingAddressSection.city.value, true)
                    }
                    false
                }
            }
            .padding(top = 16.dp),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { keyboardController?.hide() }),
        value = shippingAddressSection.city.value,
        isError = shippingAddressSection.city.isError,
        assistiveText = shippingAddressSection.city.errorMessages?.let {
            AnnotatedString(stringResource(id = it))
        },
        onValueChange = { viewModel.onCityFieldChanged(it, true) },
        label = buildAnnotatedString { append(stringResource(id = R.string.dojo_ui_sdk_card_details_checkout_field_shipping_city)) }
    )
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun PostalCodeField(
    shippingAddressSection: ShippingAddressViewState,
    viewModel: VirtualTerminalViewModel,
    coroutineScope: CoroutineScope,
    scrollToPosition: Float,
    scrollState: ScrollState,
    keyboardController: SoftwareKeyboardController?
) {
    var isTextNotFocused by remember { mutableStateOf(false) }
    val scrollOffset = with(LocalDensity.current) {
        shippingAddressSection.itemPoissonOffset.dp.toPx() + (5 * NORMAL_FILED_SIZE_DP).dp.toPx()
    }
    InputFieldWithErrorMessage(
        modifier = Modifier
            .onFocusChanged {
                isTextNotFocused = if (it.isFocused) {
                    coroutineScope.launch {
                        delay(300)
                        scrollState.animateScrollTo(
                            scrollToPosition.roundToInt() + scrollOffset.roundToInt()
                        )
                    }
                    true
                } else {
                    if (isTextNotFocused) {
                        viewModel.onValidatePostalCodeField(
                            shippingAddressSection.postalCode.value, true
                        )
                    }
                    false
                }
            }
            .padding(top = 16.dp),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { keyboardController?.hide() }),
        value = shippingAddressSection.postalCode.value,
        isError = shippingAddressSection.postalCode.isError,
        assistiveText = shippingAddressSection.postalCode.errorMessages?.let {
            AnnotatedString(stringResource(id = it))
        },
        onValueChange = { viewModel.onSPostalCodeFieldChanged(it, true) },
        label = buildAnnotatedString { append(stringResource(id = R.string.dojo_ui_sdk_card_details_checkout_field_shipping_postcode)) }
    )
}

@Composable
private fun CountryField(
    shippingAddressSection: ShippingAddressViewState,
    viewModel: VirtualTerminalViewModel
) {
    CountrySelectorField(
        modifier = Modifier
            .padding(top = 16.dp),
        label = buildAnnotatedString { append(stringResource(R.string.dojo_ui_sdk_card_details_checkout_field_shipping_country)) },
        supportedCountriesViewEntity = shippingAddressSection.supportedCountriesList,
        onCountrySelected = { viewModel.onCountrySelected(it, true) }
    )
}

@Composable
private fun DeliveryNotesField(
    shippingAddressSection: ShippingAddressViewState,
    viewModel: VirtualTerminalViewModel,
    coroutineScope: CoroutineScope,
    scrollToPosition: Float,
    scrollState: ScrollState,
) {
    val scrollOffset = with(LocalDensity.current) {
        shippingAddressSection.itemPoissonOffset.dp.toPx() + (7 * NORMAL_FILED_SIZE_DP).dp.toPx()
    }
    val label = buildAnnotatedString {
        append(stringResource(id = R.string.dojo_ui_sdk_card_details_checkout_field_shipping_delivery_notes))
        append(" ")
        withStyle(SpanStyle(DojoTheme.colors.primaryLabelTextColor.copy(alpha = ContentAlpha.medium))) { append(stringResource(id = R.string.dojo_ui_sdk_dojo_ui_sdk_card_details_checkout_optional)) }
    }
    DescriptionField(
        value = shippingAddressSection.deliveryNotes.value,
        onDescriptionChanged = { viewModel.onDeliveryNotesFieldChanged(it) },
        maxCharacters = 120,
        label = label,
        modifier = Modifier
            .padding(vertical = 16.dp)
            .onFocusChanged {
                if (it.isFocused) {
                    coroutineScope.launch {
                        delay(300)
                        scrollState.animateScrollTo(
                            scrollToPosition.roundToInt() + scrollOffset.roundToInt()
                        )
                    }
                }
            }
    )
}

@Composable
private fun AddressesAreTheSameCheckBox(
    shippingAddressSection: ShippingAddressViewState,
    viewModel: VirtualTerminalViewModel
) {
    CheckBoxItem(
        itemText = stringResource(id = shippingAddressSection.isShippingSameAsBillingCheckBox.messageText),
        onCheckedChange = {
            viewModel.onShippingSameAsBillingChecked(it)
        }
    )
}
private const val NORMAL_FILED_SIZE_DP = 100
