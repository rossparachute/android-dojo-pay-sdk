package tech.dojo.pay.uisdk.presentation.ui.virtualterminalcheckout.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import tech.dojo.pay.sdk.card.entities.CardsSchemes
import tech.dojo.pay.sdk.card.entities.DojoCardDetails
import tech.dojo.pay.sdk.card.entities.DojoCardPaymentPayLoad
import tech.dojo.pay.sdk.card.presentation.card.handler.DojoVirtualTerminalHandler
import tech.dojo.pay.uisdk.R
import tech.dojo.pay.uisdk.core.MainCoroutineScopeRule
import tech.dojo.pay.uisdk.data.entities.PaymentIntentResult
import tech.dojo.pay.uisdk.domain.GetSupportedCountriesUseCase
import tech.dojo.pay.uisdk.domain.ObservePaymentIntent
import tech.dojo.pay.uisdk.domain.ObservePaymentStatus
import tech.dojo.pay.uisdk.domain.RefreshPaymentIntentUseCase
import tech.dojo.pay.uisdk.domain.UpdatePaymentStateUseCase
import tech.dojo.pay.uisdk.domain.entities.AmountDomainEntity
import tech.dojo.pay.uisdk.domain.entities.PaymentIntentDomainEntity
import tech.dojo.pay.uisdk.presentation.ui.carddetailscheckout.entity.SupportedCountriesViewEntity
import tech.dojo.pay.uisdk.presentation.ui.virtualterminalcheckout.mapper.FullCardPaymentPayloadMapper
import tech.dojo.pay.uisdk.presentation.ui.virtualterminalcheckout.mapper.VirtualTerminalViewEntityMapper
import tech.dojo.pay.uisdk.presentation.ui.virtualterminalcheckout.state.BillingAddressViewState
import tech.dojo.pay.uisdk.presentation.ui.virtualterminalcheckout.state.CardDetailsViewState
import tech.dojo.pay.uisdk.presentation.ui.virtualterminalcheckout.state.CheckBoxItem
import tech.dojo.pay.uisdk.presentation.ui.virtualterminalcheckout.state.InputFieldState
import tech.dojo.pay.uisdk.presentation.ui.virtualterminalcheckout.state.InputFieldType
import tech.dojo.pay.uisdk.presentation.ui.virtualterminalcheckout.state.PayButtonViewState
import tech.dojo.pay.uisdk.presentation.ui.virtualterminalcheckout.state.PaymentDetailsViewState
import tech.dojo.pay.uisdk.presentation.ui.virtualterminalcheckout.state.ShippingAddressViewState
import tech.dojo.pay.uisdk.presentation.ui.virtualterminalcheckout.state.VirtualTerminalViewState
import tech.dojo.pay.uisdk.presentation.ui.virtualterminalcheckout.validator.VirtualTerminalValidator
import tech.dojo.pay.uisdk.presentation.ui.virtualterminalcheckout.viewmodel.TestData.paymentIntentDomainEntity
import tech.dojo.pay.uisdk.presentation.ui.virtualterminalcheckout.viewmodel.TestData.virtualTerminalViewState

@Suppress("LargeClass")
@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class VirtualTerminalViewModelTest {

    @get:Rule
    @ExperimentalCoroutinesApi
    val coroutineScope = MainCoroutineScopeRule()

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    // Mock dependencies
    @Mock
    private lateinit var observePaymentIntent: ObservePaymentIntent

    @Mock
    private lateinit var observePaymentStatus: ObservePaymentStatus

    @Mock
    private lateinit var updatePaymentStateUseCase: UpdatePaymentStateUseCase

    @Mock
    private lateinit var getSupportedCountriesUseCase: GetSupportedCountriesUseCase

    @Mock
    private lateinit var virtualTerminalValidator: VirtualTerminalValidator

    @Mock
    private lateinit var virtualTerminalHandler: DojoVirtualTerminalHandler

    @Mock
    private lateinit var fullCardPaymentPayloadMapper: FullCardPaymentPayloadMapper

    @Mock
    private lateinit var virtualTerminalViewEntityMapper: VirtualTerminalViewEntityMapper

    @Mock
    private lateinit var refreshPaymentIntentUseCase: RefreshPaymentIntentUseCase

    @Test
    fun `given initialize view model with Success payment intent with billing address only then content state should be emitted with billing address only`() =
        runTest {
            // arrange
            val expected = virtualTerminalViewState
            val paymentIntentFakeFlow: MutableStateFlow<PaymentIntentResult?> =
                MutableStateFlow(null)
            val paymentStateFakeFlow: MutableStateFlow<Boolean> = MutableStateFlow(false)
            whenever(observePaymentIntent.observePaymentIntent()).thenReturn(paymentIntentFakeFlow)
            whenever(observePaymentStatus.observePaymentStates()).thenReturn(paymentStateFakeFlow)
            paymentIntentFakeFlow.tryEmit(PaymentIntentResult.Success(paymentIntentDomainEntity))
            whenever(virtualTerminalViewEntityMapper.apply(any(), any())).thenReturn(
                virtualTerminalViewState
            )

            // act
            val viewModel = VirtualTerminalViewModel(
                observePaymentIntent,
                observePaymentStatus,
                updatePaymentStateUseCase,
                getSupportedCountriesUseCase,
                virtualTerminalValidator,
                virtualTerminalHandler,
                fullCardPaymentPayloadMapper,
                virtualTerminalViewEntityMapper,
                refreshPaymentIntentUseCase
            )

            // assert
            Assert.assertEquals(expected, viewModel.state.value)
        }

    @Test
    fun `given initialize view model with Success payment intent with billing and shipping addresses only then content state should be emitted with shipping address only`() =
        runTest {
            // arrange
            val expected = virtualTerminalViewState.copy(
                shippingAddressSection = virtualTerminalViewState.shippingAddressSection?.updateIsVisible(
                    true
                ),
                billingAddressSection = virtualTerminalViewState.billingAddressSection?.updateIsVisible(
                    false
                )
            )
            val viewModel = initViewModelWithPaymentIntent(expected)

            // assert
            Assert.assertEquals(expected, viewModel.state.value)
        }

    @Test
    fun `given initialize view model with Success payment intent then should start observe PaymentStatus`() =
        runTest {
            // arrange
            val paymentIntentFakeFlow: MutableStateFlow<PaymentIntentResult?> =
                MutableStateFlow(null)
            val paymentStateFakeFlow: MutableStateFlow<Boolean> = MutableStateFlow(false)
            whenever(observePaymentIntent.observePaymentIntent()).thenReturn(paymentIntentFakeFlow)
            whenever(observePaymentStatus.observePaymentStates()).thenReturn(paymentStateFakeFlow)
            paymentIntentFakeFlow.tryEmit(PaymentIntentResult.Success(paymentIntentDomainEntity))
            whenever(virtualTerminalViewEntityMapper.apply(any(), any())).thenReturn(
                virtualTerminalViewState
            )

            // act
            VirtualTerminalViewModel(
                observePaymentIntent,
                observePaymentStatus,
                updatePaymentStateUseCase,
                getSupportedCountriesUseCase,
                virtualTerminalValidator,
                virtualTerminalHandler,
                fullCardPaymentPayloadMapper,
                virtualTerminalViewEntityMapper,
                refreshPaymentIntentUseCase
            )
            // assert
            verify(observePaymentStatus).observePaymentStates()
        }

    @Test
    fun `given initialize view model with Success payment intent then getSupportedCountries from getSupportedCountriesUseCase should be called `() =
        runTest {
            // arrange
            val paymentIntentFakeFlow: MutableStateFlow<PaymentIntentResult?> =
                MutableStateFlow(null)
            val paymentStateFakeFlow: MutableStateFlow<Boolean> = MutableStateFlow(false)
            whenever(observePaymentIntent.observePaymentIntent()).thenReturn(paymentIntentFakeFlow)
            whenever(observePaymentStatus.observePaymentStates()).thenReturn(paymentStateFakeFlow)
            paymentIntentFakeFlow.tryEmit(PaymentIntentResult.Success(paymentIntentDomainEntity))
            whenever(virtualTerminalViewEntityMapper.apply(any(), any())).thenReturn(
                virtualTerminalViewState
            )

            // act
            VirtualTerminalViewModel(
                observePaymentIntent,
                observePaymentStatus,
                updatePaymentStateUseCase,
                getSupportedCountriesUseCase,
                virtualTerminalValidator,
                virtualTerminalHandler,
                fullCardPaymentPayloadMapper,
                virtualTerminalViewEntityMapper,
                refreshPaymentIntentUseCase
            )
            // assert
            verify(getSupportedCountriesUseCase).getSupportedCountries()
        }

    @Test
    fun `given onNameFieldChanged called then newValue should be emitted to the UI and isAllDataValid should be called with the new state`() =
        runTest {
            // arrange
            val newValue = "newValue"
            val initPaymentIntent = virtualTerminalViewState.copy(
                shippingAddressSection = virtualTerminalViewState.shippingAddressSection?.updateIsVisible(
                    true
                ),
                billingAddressSection = virtualTerminalViewState.billingAddressSection?.updateIsVisible(
                    false
                )
            )
            val expected = initPaymentIntent.copy(
                shippingAddressSection = initPaymentIntent.shippingAddressSection?.updateAddressName(
                    InputFieldState(newValue)
                )
            )

            val viewModel = initViewModelWithPaymentIntent(initPaymentIntent)

            // act
            viewModel.onNameFieldChanged(newValue)
            // assert
            Assert.assertEquals(expected, viewModel.state.value)
            verify(virtualTerminalValidator).isAllDataValid(expected)
        }

    @Test
    fun `given onValidateShippingNameField called then validateInputFieldIsNotEmpty should be called and state should be updated with the result from validation`() =
        runTest {
            // arrange
            val newValue = "newValue"
            val initPaymentIntent = virtualTerminalViewState.copy(
                shippingAddressSection = virtualTerminalViewState.shippingAddressSection?.updateIsVisible(
                    true
                ),
                billingAddressSection = virtualTerminalViewState.billingAddressSection?.updateIsVisible(
                    false
                )
            )
            val expected = initPaymentIntent.copy(
                shippingAddressSection = initPaymentIntent.shippingAddressSection?.updateAddressName(
                    InputFieldState(newValue)
                )
            )
            val paymentIntentFakeFlow: MutableStateFlow<PaymentIntentResult?> =
                MutableStateFlow(null)
            val paymentStateFakeFlow: MutableStateFlow<Boolean> = MutableStateFlow(false)
            whenever(observePaymentIntent.observePaymentIntent()).thenReturn(paymentIntentFakeFlow)
            paymentIntentFakeFlow.tryEmit(
                PaymentIntentResult.Success(
                    paymentIntentDomainEntity.copy(
                        collectionShippingAddressRequired = true
                    )
                )
            )
            whenever(observePaymentStatus.observePaymentStates()).thenReturn(paymentStateFakeFlow)
            whenever(virtualTerminalViewEntityMapper.apply(any(), any())).thenReturn(
                initPaymentIntent
            )
            whenever(
                virtualTerminalValidator.validateInputFieldIsNotEmpty(
                    any(), any()
                )
            ).thenReturn(InputFieldState(newValue))
            val viewModel = VirtualTerminalViewModel(
                observePaymentIntent,
                observePaymentStatus,
                updatePaymentStateUseCase,
                getSupportedCountriesUseCase,
                virtualTerminalValidator,
                virtualTerminalHandler,
                fullCardPaymentPayloadMapper,
                virtualTerminalViewEntityMapper,
                refreshPaymentIntentUseCase
            )

            // act
            viewModel.onValidateShippingNameField(newValue)
            // assert
            Assert.assertEquals(expected, viewModel.state.value)
            verify(virtualTerminalValidator).validateInputFieldIsNotEmpty(
                newValue, InputFieldType.NAME
            )
        }

    @Test
    fun `given onAddress1FieldChanged called from shipping then newValue should be emitted to the UI and isAllDataValid should be called with the new state`() =
        runTest {
            // arrange
            val newValue = "newValue"
            val initPaymentIntent = virtualTerminalViewState.copy(
                shippingAddressSection = virtualTerminalViewState.shippingAddressSection?.updateIsVisible(
                    true
                ),
                billingAddressSection = virtualTerminalViewState.billingAddressSection?.updateIsVisible(
                    false
                )
            )
            val expected = initPaymentIntent.copy(
                shippingAddressSection = initPaymentIntent.shippingAddressSection?.updateAddressLine1(
                    InputFieldState(newValue)
                )
            )
            val viewModel = initViewModelWithPaymentIntent(initPaymentIntent)

            // act
            viewModel.onAddress1FieldChanged(newValue, true)
            // assert
            Assert.assertEquals(expected, viewModel.state.value)
            verify(virtualTerminalValidator).isAllDataValid(expected)
        }

    @Test
    fun `given onAddress1FieldChanged called from Billing  then newValue should be emitted to the UI and isAllDataValid should be called with the new state`() =
        runTest {
            // arrange
            val newValue = "newValue"
            val initPaymentIntent = virtualTerminalViewState.copy(
                shippingAddressSection = virtualTerminalViewState.shippingAddressSection?.updateIsVisible(
                    true
                ),
                billingAddressSection = virtualTerminalViewState.billingAddressSection?.updateIsVisible(
                    true
                )
            )
            val expected = initPaymentIntent.copy(
                billingAddressSection = initPaymentIntent.billingAddressSection?.updateAddressLine1(
                    InputFieldState(newValue)
                )
            )
            val viewModel = initViewModelWithPaymentIntent(initPaymentIntent)

            // act
            viewModel.onAddress1FieldChanged(newValue, false)
            // assert
            Assert.assertEquals(expected, viewModel.state.value)
            verify(virtualTerminalValidator).isAllDataValid(expected)
        }

    @Test
    fun `given onValidateAddress1Field called from shipping then validateInputFieldIsNotEmpty should be called and state should be updated with the result from validation`() =
        runTest {
            // arrange
            val newValue = "newValue"
            val initPaymentIntent = virtualTerminalViewState.copy(
                shippingAddressSection = virtualTerminalViewState.shippingAddressSection?.updateIsVisible(
                    true
                ),
                billingAddressSection = virtualTerminalViewState.billingAddressSection?.updateIsVisible(
                    false
                )
            )
            val expected = initPaymentIntent.copy(
                shippingAddressSection = initPaymentIntent.shippingAddressSection?.updateAddressLine1(
                    InputFieldState(newValue)
                )
            )
            val paymentIntentFakeFlow: MutableStateFlow<PaymentIntentResult?> =
                MutableStateFlow(null)
            val paymentStateFakeFlow: MutableStateFlow<Boolean> = MutableStateFlow(false)
            whenever(observePaymentIntent.observePaymentIntent()).thenReturn(paymentIntentFakeFlow)
            paymentIntentFakeFlow.tryEmit(
                PaymentIntentResult.Success(
                    paymentIntentDomainEntity.copy(
                        collectionShippingAddressRequired = true
                    )
                )
            )
            whenever(observePaymentStatus.observePaymentStates()).thenReturn(paymentStateFakeFlow)
            whenever(virtualTerminalViewEntityMapper.apply(any(), any())).thenReturn(
                initPaymentIntent
            )
            whenever(
                virtualTerminalValidator.validateInputFieldIsNotEmpty(
                    any(), any()
                )
            ).thenReturn(InputFieldState(newValue))
            val viewModel = VirtualTerminalViewModel(
                observePaymentIntent,
                observePaymentStatus,
                updatePaymentStateUseCase,
                getSupportedCountriesUseCase,
                virtualTerminalValidator,
                virtualTerminalHandler,
                fullCardPaymentPayloadMapper,
                virtualTerminalViewEntityMapper,
                refreshPaymentIntentUseCase
            )

            // act
            viewModel.onValidateAddress1Field(newValue, true)
            // assert
            Assert.assertEquals(expected, viewModel.state.value)
            verify(virtualTerminalValidator).validateInputFieldIsNotEmpty(
                newValue, InputFieldType.ADDRESS1
            )
        }

    @Test
    fun `given onValidateAddress1Field called from Billing then validateInputFieldIsNotEmpty should be called and state should be updated with the result from validation`() =
        runTest {
            // arrange
            val newValue = "newValue"
            val initPaymentIntent = virtualTerminalViewState.copy(
                shippingAddressSection = virtualTerminalViewState.shippingAddressSection?.updateIsVisible(
                    true
                ),
                billingAddressSection = virtualTerminalViewState.billingAddressSection?.updateIsVisible(
                    true
                )
            )
            val expected = initPaymentIntent.copy(
                billingAddressSection = initPaymentIntent.billingAddressSection?.updateAddressLine1(
                    InputFieldState(newValue)
                )
            )
            val paymentIntentFakeFlow: MutableStateFlow<PaymentIntentResult?> =
                MutableStateFlow(null)
            val paymentStateFakeFlow: MutableStateFlow<Boolean> = MutableStateFlow(false)
            whenever(observePaymentIntent.observePaymentIntent()).thenReturn(paymentIntentFakeFlow)
            paymentIntentFakeFlow.tryEmit(
                PaymentIntentResult.Success(
                    paymentIntentDomainEntity.copy(
                        collectionShippingAddressRequired = true
                    )
                )
            )
            whenever(observePaymentStatus.observePaymentStates()).thenReturn(paymentStateFakeFlow)
            whenever(virtualTerminalViewEntityMapper.apply(any(), any())).thenReturn(
                initPaymentIntent
            )
            whenever(
                virtualTerminalValidator.validateInputFieldIsNotEmpty(
                    any(), any()
                )
            ).thenReturn(InputFieldState(newValue))
            val viewModel = VirtualTerminalViewModel(
                observePaymentIntent,
                observePaymentStatus,
                updatePaymentStateUseCase,
                getSupportedCountriesUseCase,
                virtualTerminalValidator,
                virtualTerminalHandler,
                fullCardPaymentPayloadMapper,
                virtualTerminalViewEntityMapper,
                refreshPaymentIntentUseCase
            )

            // act
            viewModel.onValidateAddress1Field(newValue, false)
            // assert
            Assert.assertEquals(expected, viewModel.state.value)
            verify(virtualTerminalValidator).validateInputFieldIsNotEmpty(
                newValue, InputFieldType.ADDRESS1
            )
        }

    @Test
    fun `given onAddress2FieldChanged called from shipping then newValue should be emitted to the UI`() =
        runTest {
            // arrange
            val newValue = "newValue"
            val initPaymentIntent = virtualTerminalViewState.copy(
                shippingAddressSection = virtualTerminalViewState.shippingAddressSection?.updateIsVisible(
                    true
                ),
                billingAddressSection = virtualTerminalViewState.billingAddressSection?.updateIsVisible(
                    false
                )
            )
            val expected = initPaymentIntent.copy(
                shippingAddressSection = initPaymentIntent.shippingAddressSection?.updateAddressLine2(
                    InputFieldState(newValue)
                )
            )
            val viewModel = initViewModelWithPaymentIntent(initPaymentIntent)

            // act
            viewModel.onAddress2FieldChanged(newValue, true)
            // assert
            Assert.assertEquals(expected, viewModel.state.value)
        }

    @Test
    fun `given onAddress2FieldChanged called from Billing then newValue should be emitted to the UI `() =
        runTest {
            // arrange
            val newValue = "newValue"
            val initPaymentIntent = virtualTerminalViewState.copy(
                shippingAddressSection = virtualTerminalViewState.shippingAddressSection?.updateIsVisible(
                    true
                ),
                billingAddressSection = virtualTerminalViewState.billingAddressSection?.updateIsVisible(
                    true
                )
            )
            val expected = initPaymentIntent.copy(
                billingAddressSection = initPaymentIntent.billingAddressSection?.updateAddressLine2(
                    InputFieldState(newValue)
                )
            )
            val viewModel = initViewModelWithPaymentIntent(initPaymentIntent)

            // act
            viewModel.onAddress2FieldChanged(newValue, false)
            // assert
            Assert.assertEquals(expected, viewModel.state.value)
        }

    @Test
    fun `given onCityFieldChanged called from shipping then newValue should be emitted to the UI and isAllDataValid should be called with the new state`() =
        runTest {
            // arrange
            val newValue = "newValue"
            val initPaymentIntent = virtualTerminalViewState.copy(
                shippingAddressSection = virtualTerminalViewState.shippingAddressSection?.updateIsVisible(
                    true
                ),
                billingAddressSection = virtualTerminalViewState.billingAddressSection?.updateIsVisible(
                    false
                )
            )
            val expected = initPaymentIntent.copy(
                shippingAddressSection = initPaymentIntent.shippingAddressSection?.updateCity(
                    InputFieldState(newValue)
                )
            )
            val viewModel = initViewModelWithPaymentIntent(initPaymentIntent)

            // act
            viewModel.onCityFieldChanged(newValue, true)
            // assert
            Assert.assertEquals(expected, viewModel.state.value)
            verify(virtualTerminalValidator).isAllDataValid(expected)
        }

    @Test
    fun `given onCityFieldChanged called from Billing then newValue should be emitted to the UI and isAllDataValid should be called with the new state`() =
        runTest {
            // arrange
            val newValue = "newValue"
            val initPaymentIntent = virtualTerminalViewState.copy(
                shippingAddressSection = virtualTerminalViewState.shippingAddressSection?.updateIsVisible(
                    true
                ),
                billingAddressSection = virtualTerminalViewState.billingAddressSection?.updateIsVisible(
                    true
                )
            )
            val expected = initPaymentIntent.copy(
                billingAddressSection = initPaymentIntent.billingAddressSection?.updateCity(
                    InputFieldState(newValue)
                )
            )
            val viewModel = initViewModelWithPaymentIntent(initPaymentIntent)

            // act
            viewModel.onCityFieldChanged(newValue, false)
            // assert
            Assert.assertEquals(expected, viewModel.state.value)
            verify(virtualTerminalValidator).isAllDataValid(expected)
        }

    @Test
    fun `given onValidateCityField called from shipping then validateInputFieldIsNotEmpty should be called and state should be updated with the result from validation`() =
        runTest {
            // arrange
            val newValue = "newValue"
            val initPaymentIntent = virtualTerminalViewState.copy(
                shippingAddressSection = virtualTerminalViewState.shippingAddressSection?.updateIsVisible(
                    true
                ),
                billingAddressSection = virtualTerminalViewState.billingAddressSection?.updateIsVisible(
                    false
                )
            )
            val expected = initPaymentIntent.copy(
                shippingAddressSection = initPaymentIntent.shippingAddressSection?.updateCity(
                    InputFieldState(newValue)
                )
            )
            val paymentIntentFakeFlow: MutableStateFlow<PaymentIntentResult?> =
                MutableStateFlow(null)
            val paymentStateFakeFlow: MutableStateFlow<Boolean> = MutableStateFlow(false)
            whenever(observePaymentIntent.observePaymentIntent()).thenReturn(paymentIntentFakeFlow)
            paymentIntentFakeFlow.tryEmit(
                PaymentIntentResult.Success(
                    paymentIntentDomainEntity.copy(
                        collectionShippingAddressRequired = true
                    )
                )
            )
            whenever(observePaymentStatus.observePaymentStates()).thenReturn(paymentStateFakeFlow)
            whenever(virtualTerminalViewEntityMapper.apply(any(), any())).thenReturn(
                initPaymentIntent
            )
            whenever(
                virtualTerminalValidator.validateInputFieldIsNotEmpty(
                    any(), any()
                )
            ).thenReturn(InputFieldState(newValue))
            val viewModel = VirtualTerminalViewModel(
                observePaymentIntent,
                observePaymentStatus,
                updatePaymentStateUseCase,
                getSupportedCountriesUseCase,
                virtualTerminalValidator,
                virtualTerminalHandler,
                fullCardPaymentPayloadMapper,
                virtualTerminalViewEntityMapper,
                refreshPaymentIntentUseCase
            )

            // act
            viewModel.onValidateCityField(newValue, true)
            // assert
            Assert.assertEquals(expected, viewModel.state.value)
            verify(virtualTerminalValidator).validateInputFieldIsNotEmpty(
                newValue, InputFieldType.CITY
            )
        }

    @Test
    fun `given onValidateCityField called from Billing then validateInputFieldIsNotEmpty should be called and state should be updated with the result from validation`() =
        runTest {
            // arrange
            val newValue = "newValue"
            val initPaymentIntent = virtualTerminalViewState.copy(
                shippingAddressSection = virtualTerminalViewState.shippingAddressSection?.updateIsVisible(
                    true
                ),
                billingAddressSection = virtualTerminalViewState.billingAddressSection?.updateIsVisible(
                    true
                )
            )
            val expected = initPaymentIntent.copy(
                billingAddressSection = initPaymentIntent.billingAddressSection?.updateCity(
                    InputFieldState(newValue)
                )
            )
            val paymentIntentFakeFlow: MutableStateFlow<PaymentIntentResult?> =
                MutableStateFlow(null)
            val paymentStateFakeFlow: MutableStateFlow<Boolean> = MutableStateFlow(false)
            whenever(observePaymentIntent.observePaymentIntent()).thenReturn(paymentIntentFakeFlow)
            paymentIntentFakeFlow.tryEmit(
                PaymentIntentResult.Success(
                    paymentIntentDomainEntity.copy(
                        collectionShippingAddressRequired = true
                    )
                )
            )
            whenever(observePaymentStatus.observePaymentStates()).thenReturn(paymentStateFakeFlow)
            whenever(virtualTerminalViewEntityMapper.apply(any(), any())).thenReturn(
                initPaymentIntent
            )
            whenever(
                virtualTerminalValidator.validateInputFieldIsNotEmpty(
                    any(), any()
                )
            ).thenReturn(InputFieldState(newValue))
            val viewModel = VirtualTerminalViewModel(
                observePaymentIntent,
                observePaymentStatus,
                updatePaymentStateUseCase,
                getSupportedCountriesUseCase,
                virtualTerminalValidator,
                virtualTerminalHandler,
                fullCardPaymentPayloadMapper,
                virtualTerminalViewEntityMapper,
                refreshPaymentIntentUseCase
            )

            // act
            viewModel.onValidateCityField(newValue, false)
            // assert
            Assert.assertEquals(expected, viewModel.state.value)
            verify(virtualTerminalValidator).validateInputFieldIsNotEmpty(
                newValue, InputFieldType.CITY
            )
        }

    @Test
    fun `given onSPostalCodeFieldChanged called from shipping then newValue should be emitted to the UI and isAllDataValid should be called with the new state`() =
        runTest {
            // arrange
            val newValue = "newValue"
            val initPaymentIntent = virtualTerminalViewState.copy(
                shippingAddressSection = virtualTerminalViewState.shippingAddressSection?.updateIsVisible(
                    true
                ),
                billingAddressSection = virtualTerminalViewState.billingAddressSection?.updateIsVisible(
                    false
                )
            )
            val expected = initPaymentIntent.copy(
                shippingAddressSection = initPaymentIntent.shippingAddressSection?.updatePostalCode(
                    InputFieldState(newValue)
                )
            )
            val viewModel = initViewModelWithPaymentIntent(initPaymentIntent)

            // act
            viewModel.onSPostalCodeFieldChanged(newValue, true)
            // assert
            Assert.assertEquals(expected, viewModel.state.value)
            verify(virtualTerminalValidator).isAllDataValid(expected)
        }

    @Test
    fun `given onSPostalCodeFieldChanged called from Billing  then newValue should be emitted to the UI and isAllDataValid should be called with the new state`() =
        runTest {
            // arrange
            val newValue = "newValue"
            val initPaymentIntent = virtualTerminalViewState.copy(
                shippingAddressSection = virtualTerminalViewState.shippingAddressSection?.updateIsVisible(
                    true
                ),
                billingAddressSection = virtualTerminalViewState.billingAddressSection?.updateIsVisible(
                    true
                )
            )
            val expected = initPaymentIntent.copy(
                billingAddressSection = initPaymentIntent.billingAddressSection?.updatePostalCode(
                    InputFieldState(newValue)
                )
            )
            val viewModel = initViewModelWithPaymentIntent(initPaymentIntent)

            // act
            viewModel.onSPostalCodeFieldChanged(newValue, false)
            // assert
            Assert.assertEquals(expected, viewModel.state.value)
            verify(virtualTerminalValidator).isAllDataValid(expected)
        }

    @Test
    fun `given onValidatePostalCodeField called from shipping then validateInputFieldIsNotEmpty should be called and state should be updated with the result from validation`() =
        runTest {
            // arrange
            val newValue = "newValue"
            val initPaymentIntent = virtualTerminalViewState.copy(
                shippingAddressSection = virtualTerminalViewState.shippingAddressSection?.updateIsVisible(
                    true
                ),
                billingAddressSection = virtualTerminalViewState.billingAddressSection?.updateIsVisible(
                    false
                )
            )
            val expected = initPaymentIntent.copy(
                shippingAddressSection = initPaymentIntent.shippingAddressSection?.updatePostalCode(
                    InputFieldState(newValue)
                )
            )
            val paymentIntentFakeFlow: MutableStateFlow<PaymentIntentResult?> =
                MutableStateFlow(null)
            val paymentStateFakeFlow: MutableStateFlow<Boolean> = MutableStateFlow(false)
            whenever(observePaymentIntent.observePaymentIntent()).thenReturn(paymentIntentFakeFlow)
            paymentIntentFakeFlow.tryEmit(
                PaymentIntentResult.Success(
                    paymentIntentDomainEntity.copy(
                        collectionShippingAddressRequired = true
                    )
                )
            )
            whenever(observePaymentStatus.observePaymentStates()).thenReturn(paymentStateFakeFlow)
            whenever(virtualTerminalViewEntityMapper.apply(any(), any())).thenReturn(
                initPaymentIntent
            )
            whenever(
                virtualTerminalValidator.validateInputFieldIsNotEmpty(
                    any(), any()
                )
            ).thenReturn(InputFieldState(newValue))
            val viewModel = VirtualTerminalViewModel(
                observePaymentIntent,
                observePaymentStatus,
                updatePaymentStateUseCase,
                getSupportedCountriesUseCase,
                virtualTerminalValidator,
                virtualTerminalHandler,
                fullCardPaymentPayloadMapper,
                virtualTerminalViewEntityMapper,
                refreshPaymentIntentUseCase
            )

            // act
            viewModel.onValidatePostalCodeField(newValue, true)
            // assert
            Assert.assertEquals(expected, viewModel.state.value)
            verify(virtualTerminalValidator).validateInputFieldIsNotEmpty(
                newValue, InputFieldType.POSTAL_CODE
            )
        }

    @Test
    fun `given onValidatePostalCodeField called from Billing then validateInputFieldIsNotEmpty should be called and state should be updated with the result from validation`() =
        runTest {
            // arrange
            val newValue = "newValue"
            val initPaymentIntent = virtualTerminalViewState.copy(
                shippingAddressSection = virtualTerminalViewState.shippingAddressSection?.updateIsVisible(
                    true
                ),
                billingAddressSection = virtualTerminalViewState.billingAddressSection?.updateIsVisible(
                    true
                )
            )
            val expected = initPaymentIntent.copy(
                billingAddressSection = initPaymentIntent.billingAddressSection?.updatePostalCode(
                    InputFieldState(newValue)
                )
            )
            val paymentIntentFakeFlow: MutableStateFlow<PaymentIntentResult?> =
                MutableStateFlow(null)
            val paymentStateFakeFlow: MutableStateFlow<Boolean> = MutableStateFlow(false)
            whenever(observePaymentIntent.observePaymentIntent()).thenReturn(paymentIntentFakeFlow)
            paymentIntentFakeFlow.tryEmit(
                PaymentIntentResult.Success(
                    paymentIntentDomainEntity.copy(
                        collectionShippingAddressRequired = true
                    )
                )
            )
            whenever(observePaymentStatus.observePaymentStates()).thenReturn(paymentStateFakeFlow)
            whenever(virtualTerminalViewEntityMapper.apply(any(), any())).thenReturn(
                initPaymentIntent
            )
            whenever(
                virtualTerminalValidator.validateInputFieldIsNotEmpty(
                    any(), any()
                )
            ).thenReturn(InputFieldState(newValue))
            val viewModel = VirtualTerminalViewModel(
                observePaymentIntent,
                observePaymentStatus,
                updatePaymentStateUseCase,
                getSupportedCountriesUseCase,
                virtualTerminalValidator,
                virtualTerminalHandler,
                fullCardPaymentPayloadMapper,
                virtualTerminalViewEntityMapper,
                refreshPaymentIntentUseCase
            )

            // act
            viewModel.onValidatePostalCodeField(newValue, false)
            // assert
            Assert.assertEquals(expected, viewModel.state.value)
            verify(virtualTerminalValidator).validateInputFieldIsNotEmpty(
                newValue, InputFieldType.POSTAL_CODE
            )
        }

    @Test
    fun `given onCountrySelected called from shipping then newValue should be emitted to the UI `() =
        runTest {
            // arrange
            val newValue = SupportedCountriesViewEntity(
                countryCode = "newValue",
                countryName = "newValue",
                isPostalCodeEnabled = true
            )
            val initPaymentIntent = virtualTerminalViewState.copy(
                shippingAddressSection = virtualTerminalViewState.shippingAddressSection?.updateIsVisible(
                    true
                ),
                billingAddressSection = virtualTerminalViewState.billingAddressSection?.updateIsVisible(
                    false
                )
            )
            val expected = initPaymentIntent.copy(
                shippingAddressSection = initPaymentIntent.shippingAddressSection?.updateCurrentSelectedCountry(
                    newValue
                )
            )
            val viewModel = initViewModelWithPaymentIntent(initPaymentIntent)

            // act
            viewModel.onCountrySelected(newValue, true)
            // assert
            Assert.assertEquals(expected, viewModel.state.value)
        }

    @Test
    fun `given onCountrySelected called from Billing then newValue should be emitted to the UI `() =
        runTest {
            // arrange
            val newValue = SupportedCountriesViewEntity(
                countryCode = "newValue",
                countryName = "newValue",
                isPostalCodeEnabled = true
            )
            val initPaymentIntent = virtualTerminalViewState.copy(
                shippingAddressSection = virtualTerminalViewState.shippingAddressSection?.updateIsVisible(
                    true
                ),
                billingAddressSection = virtualTerminalViewState.billingAddressSection?.updateIsVisible(
                    false
                )
            )
            val expected = initPaymentIntent.copy(
                billingAddressSection = initPaymentIntent.billingAddressSection?.updateCurrentSelectedCountry(
                    newValue
                )
            )
            val viewModel = initViewModelWithPaymentIntent(initPaymentIntent)

            // act
            viewModel.onCountrySelected(newValue, false)
            // assert
            Assert.assertEquals(expected, viewModel.state.value)
        }

    @Test
    fun `given onDeliveryNotesFieldChanged from shipping called then newValue should be emitted to the UI `() =
        runTest {
            // arrange
            val newValue = "newValue "
            val initPaymentIntent = virtualTerminalViewState.copy(
                shippingAddressSection = virtualTerminalViewState.shippingAddressSection?.updateIsVisible(
                    true
                ),
                billingAddressSection = virtualTerminalViewState.billingAddressSection?.updateIsVisible(
                    false
                )
            )
            val expected = initPaymentIntent.copy(
                shippingAddressSection = initPaymentIntent.shippingAddressSection?.updateDeliveryNotes(
                    InputFieldState(newValue)
                )
            )
            val viewModel = initViewModelWithPaymentIntent(initPaymentIntent)

            // act
            viewModel.onDeliveryNotesFieldChanged(newValue)
            // assert
            Assert.assertEquals(expected, viewModel.state.value)
        }

    @Test
    fun `given onShippingSameAsBillingChecked from shipping called with true then billing should be hidden and update views offset `() =
        runTest {
            // arrange
            val newValue = true
            val initPaymentIntent = virtualTerminalViewState.copy(
                shippingAddressSection = virtualTerminalViewState.shippingAddressSection?.updateIsVisible(
                    true
                ),
                billingAddressSection = virtualTerminalViewState.billingAddressSection?.updateIsVisible(
                    true
                )
            )
            val expected = initPaymentIntent.copy(
                shippingAddressSection = initPaymentIntent.shippingAddressSection?.updateIsShippingSameAsBillingCheckBox(
                    CheckBoxItem(
                        messageText = R.string.dojo_ui_sdk_card_details_checkout_billing_same_as_shipping,
                        isChecked = newValue,
                        isVisible = true
                    )
                ),
                billingAddressSection = initPaymentIntent.billingAddressSection
                    ?.updateIsVisible(false)
                    ?.updateItemPoissonOffset(0),
                cardDetailsSection = initPaymentIntent.cardDetailsSection?.updateItemPoissonOffset(
                    SECOND_SECTION_WITH_SHIPPING_OFF_SET_DP
                )
            )
            val viewModel = initViewModelWithPaymentIntent(initPaymentIntent)

            // act
            viewModel.onShippingSameAsBillingChecked(newValue)
            // assert
            Assert.assertEquals(expected, viewModel.state.value)
        }

    @Test
    fun `given onShippingSameAsBillingChecked from shipping called with false then billing should be visible  and update views offset `() =
        runTest {
            // arrange
            val newValue = false
            val initPaymentIntent = virtualTerminalViewState.copy(
                shippingAddressSection = virtualTerminalViewState.shippingAddressSection?.updateIsVisible(
                    true
                ),
                billingAddressSection = virtualTerminalViewState.billingAddressSection?.updateIsVisible(
                    true
                )
            )
            val expected = initPaymentIntent.copy(
                shippingAddressSection = initPaymentIntent.shippingAddressSection?.updateIsShippingSameAsBillingCheckBox(
                    CheckBoxItem(
                        messageText = R.string.dojo_ui_sdk_card_details_checkout_billing_same_as_shipping,
                        isChecked = newValue,
                        isVisible = true
                    )
                ),
                billingAddressSection = initPaymentIntent.billingAddressSection
                    ?.updateIsVisible(true)
                    ?.updateItemPoissonOffset(SECOND_SECTION_WITH_SHIPPING_OFF_SET_DP),
                cardDetailsSection = initPaymentIntent.cardDetailsSection?.updateItemPoissonOffset(
                    THIRD_SECTION_OFF_SET_DP
                )
            )
            val viewModel = initViewModelWithPaymentIntent(initPaymentIntent)

            // act
            viewModel.onShippingSameAsBillingChecked(newValue)
            // assert
            Assert.assertEquals(expected, viewModel.state.value)
        }

    @Test
    fun `given onCardHolderChanged called then newValue should be emitted to the UI and isAllDataValid should be called with the new state`() =
        runTest {
            // arrange
            val newValue = "newValue"
            val initPaymentIntent = virtualTerminalViewState.copy(
                shippingAddressSection = virtualTerminalViewState.shippingAddressSection?.updateIsVisible(
                    true
                ),
                billingAddressSection = virtualTerminalViewState.billingAddressSection?.updateIsVisible(
                    true
                )
            )
            val expected = initPaymentIntent.copy(
                cardDetailsSection = initPaymentIntent.cardDetailsSection?.updateCardHolderInputField(
                    InputFieldState(newValue)
                )
            )
            val viewModel = initViewModelWithPaymentIntent(initPaymentIntent)

            // act
            viewModel.onCardHolderChanged(newValue)
            // assert
            Assert.assertEquals(expected, viewModel.state.value)
            verify(virtualTerminalValidator).isAllDataValid(expected)
        }

    @Test
    fun `given onValidateCardHolder called then validateInputFieldIsNotEmpty should be called and state should be updated with the result from validation`() =
        runTest {
            // arrange
            val newValue = "newValue"
            val initPaymentIntent = virtualTerminalViewState.copy(
                shippingAddressSection = virtualTerminalViewState.shippingAddressSection?.updateIsVisible(
                    true
                ),
                billingAddressSection = virtualTerminalViewState.billingAddressSection?.updateIsVisible(
                    true
                )
            )
            val expected = initPaymentIntent.copy(
                cardDetailsSection = initPaymentIntent.cardDetailsSection?.updateCardHolderInputField(
                    InputFieldState(newValue)
                )
            )
            whenever(
                virtualTerminalValidator.validateInputFieldIsNotEmpty(
                    any(), any()
                )
            ).thenReturn(InputFieldState(newValue))
            val viewModel = initViewModelWithPaymentIntent(initPaymentIntent)

            // act
            viewModel.onValidateCardHolder(newValue)
            // assert
            Assert.assertEquals(expected, viewModel.state.value)
            verify(virtualTerminalValidator).validateInputFieldIsNotEmpty(
                newValue, InputFieldType.CARD_HOLDER_NAME
            )
        }

    @Test
    fun `given onCardNumberChanged called then newValue should be emitted to the UI and isAllDataValid should be called with the new state`() =
        runTest {
            // arrange
            val newValue = "newValue"
            val initPaymentIntent = virtualTerminalViewState.copy(
                shippingAddressSection = virtualTerminalViewState.shippingAddressSection?.updateIsVisible(
                    true
                ),
                billingAddressSection = virtualTerminalViewState.billingAddressSection?.updateIsVisible(
                    true
                )
            )
            val expected = initPaymentIntent.copy(
                cardDetailsSection = initPaymentIntent.cardDetailsSection?.updateCardNumberInputField(
                    InputFieldState(newValue)
                )
            )
            val viewModel = initViewModelWithPaymentIntent(initPaymentIntent)

            // act
            viewModel.onCardNumberChanged(newValue)
            // assert
            Assert.assertEquals(expected, viewModel.state.value)
            verify(virtualTerminalValidator).isAllDataValid(expected)
        }

    @Test
    fun `given onValidateCardNumber called then validateCardNumberInputField should be called and state should be updated with the result from validation`() =
        runTest {
            // arrange
            val newValue = "newValue"
            val initPaymentIntent = virtualTerminalViewState.copy(
                shippingAddressSection = virtualTerminalViewState.shippingAddressSection?.updateIsVisible(
                    true
                ),
                billingAddressSection = virtualTerminalViewState.billingAddressSection?.updateIsVisible(
                    true
                )
            )
            val expected = initPaymentIntent.copy(
                cardDetailsSection = initPaymentIntent.cardDetailsSection?.updateCardNumberInputField(
                    InputFieldState(newValue)
                )
            )
            whenever(
                virtualTerminalValidator.validateCardNumberInputField(any())
            ).thenReturn(InputFieldState(newValue))
            val viewModel = initViewModelWithPaymentIntent(initPaymentIntent)

            // act
            viewModel.onValidateCardNumber(newValue)
            // assert
            Assert.assertEquals(expected, viewModel.state.value)
            verify(virtualTerminalValidator).validateCardNumberInputField(newValue)
        }

    @Test
    fun `given onCardCvvChanged called then newValue should be emitted to the UI and isAllDataValid should be called with the new state`() =
        runTest {
            // arrange
            val newValue = "newValue"
            val initPaymentIntent = virtualTerminalViewState.copy(
                shippingAddressSection = virtualTerminalViewState.shippingAddressSection?.updateIsVisible(
                    true
                ),
                billingAddressSection = virtualTerminalViewState.billingAddressSection?.updateIsVisible(
                    true
                )
            )
            val expected = initPaymentIntent.copy(
                cardDetailsSection = initPaymentIntent.cardDetailsSection?.updateCvvInputFieldState(
                    InputFieldState(newValue)
                )
            )
            val viewModel = initViewModelWithPaymentIntent(initPaymentIntent)

            // act
            viewModel.onCardCvvChanged(newValue)
            // assert
            Assert.assertEquals(expected, viewModel.state.value)
            verify(virtualTerminalValidator).isAllDataValid(expected)
        }

    @Test
    fun `given onValidateCvv called then validateCardNumberInputField should be called and state should be updated with the result from validation`() =
        runTest {
            // arrange
            val newValue = "newValue"
            val initPaymentIntent = virtualTerminalViewState.copy(
                shippingAddressSection = virtualTerminalViewState.shippingAddressSection?.updateIsVisible(
                    true
                ),
                billingAddressSection = virtualTerminalViewState.billingAddressSection?.updateIsVisible(
                    true
                )
            )
            val expected = initPaymentIntent.copy(
                cardDetailsSection = initPaymentIntent.cardDetailsSection?.updateCvvInputFieldState(
                    InputFieldState(newValue)
                )
            )
            whenever(
                virtualTerminalValidator.validateCVVInputField(any())
            ).thenReturn(InputFieldState(newValue))
            val viewModel = initViewModelWithPaymentIntent(initPaymentIntent)

            // act
            viewModel.onValidateCvv(newValue)
            // assert
            Assert.assertEquals(expected, viewModel.state.value)
            verify(virtualTerminalValidator).validateCVVInputField(newValue)
        }

    @Test
    fun `given onCardDateChanged called then newValue should be emitted to the UI and isAllDataValid should be called with the new state`() =
        runTest {
            // arrange
            val newValue = "newValue"
            val initPaymentIntent = virtualTerminalViewState.copy(
                shippingAddressSection = virtualTerminalViewState.shippingAddressSection?.updateIsVisible(
                    true
                ),
                billingAddressSection = virtualTerminalViewState.billingAddressSection?.updateIsVisible(
                    true
                )
            )
            val expected = initPaymentIntent.copy(
                cardDetailsSection = initPaymentIntent.cardDetailsSection?.updateCardExpireDateInputField(
                    InputFieldState(newValue)
                )
            )
            val viewModel = initViewModelWithPaymentIntent(initPaymentIntent)

            // act
            viewModel.onCardDateChanged(newValue)
            // assert
            Assert.assertEquals(expected, viewModel.state.value)
            verify(virtualTerminalValidator).isAllDataValid(expected)
        }

    @Test
    fun `given onValidateCardDate called then validateCardNumberInputField should be called and state should be updated with the result from validation`() =
        runTest {
            // arrange
            val newValue = "newValue"
            val initPaymentIntent = virtualTerminalViewState.copy(
                shippingAddressSection = virtualTerminalViewState.shippingAddressSection?.updateIsVisible(
                    true
                ),
                billingAddressSection = virtualTerminalViewState.billingAddressSection?.updateIsVisible(
                    true
                )
            )
            val expected = initPaymentIntent.copy(
                cardDetailsSection = initPaymentIntent.cardDetailsSection?.updateCardExpireDateInputField(
                    InputFieldState(newValue)
                )
            )
            whenever(
                virtualTerminalValidator.validateExpireDateInputField(any())
            ).thenReturn(InputFieldState(newValue))
            val viewModel = initViewModelWithPaymentIntent(initPaymentIntent)

            // act
            viewModel.onValidateCardDate(newValue)
            // assert
            Assert.assertEquals(expected, viewModel.state.value)
            verify(virtualTerminalValidator).validateExpireDateInputField(newValue)
        }

    @Test
    fun `given onEmailChanged called then newValue should be emitted to the UI and isAllDataValid should be called with the new state`() =
        runTest {
            // arrange
            val newValue = "newValue"
            val initPaymentIntent = virtualTerminalViewState.copy(
                shippingAddressSection = virtualTerminalViewState.shippingAddressSection?.updateIsVisible(
                    true
                ),
                billingAddressSection = virtualTerminalViewState.billingAddressSection?.updateIsVisible(
                    true
                )
            )
            val expected = initPaymentIntent.copy(
                cardDetailsSection = initPaymentIntent.cardDetailsSection?.updateEmailInputField(
                    InputFieldState(newValue)
                )
            )
            val viewModel = initViewModelWithPaymentIntent(initPaymentIntent)

            // act
            viewModel.onEmailChanged(newValue)
            // assert
            Assert.assertEquals(expected, viewModel.state.value)
            verify(virtualTerminalValidator).isAllDataValid(expected)
        }

    @Test
    fun `given onValidateEmail called then validateCardNumberInputField should be called and state should be updated with the result from validation`() =
        runTest {
            // arrange
            val newValue = "newValue"
            val initPaymentIntent = virtualTerminalViewState.copy(
                shippingAddressSection = virtualTerminalViewState.shippingAddressSection?.updateIsVisible(
                    true
                ),
                billingAddressSection = virtualTerminalViewState.billingAddressSection?.updateIsVisible(
                    true
                )
            )
            val expected = initPaymentIntent.copy(
                cardDetailsSection = initPaymentIntent.cardDetailsSection?.updateEmailInputField(
                    InputFieldState(newValue)
                )
            )
            whenever(
                virtualTerminalValidator.validateEmailInputField(any())
            ).thenReturn(InputFieldState(newValue))
            val viewModel = initViewModelWithPaymentIntent(initPaymentIntent)

            // act
            viewModel.onValidateEmail(newValue)
            // assert
            Assert.assertEquals(expected, viewModel.state.value)
            verify(virtualTerminalValidator).validateEmailInputField(newValue)
        }

    @Test
    fun `given onPayClicked called then loading state should be emitted and ,  refreshPaymentIntent from refreshPaymentIntentUseCase and executeVirtualTerminalPayment from handler should be called `() =
        runTest {
            // arrange
            val address = DojoCardPaymentPayLoad.FullCardPaymentPayload(
                DojoCardDetails(
                    cardNumber = "cardNumber"
                )
            )
            val initPaymentIntent = virtualTerminalViewState.copy(
                shippingAddressSection = virtualTerminalViewState.shippingAddressSection?.updateIsVisible(
                    true
                ),
                billingAddressSection = virtualTerminalViewState.billingAddressSection?.updateIsVisible(
                    true
                ),
                payButtonSection = PayButtonViewState(
                    isEnabled = true,
                    isLoading = false
                )
            )
            val expected = initPaymentIntent.copy(
                payButtonSection = PayButtonViewState(
                    isEnabled = true,
                    isLoading = true
                )
            )
            whenever(
                virtualTerminalValidator.isAllDataValid(any())
            ).thenReturn(true)

            whenever(
                fullCardPaymentPayloadMapper.apply(any())
            ).thenReturn(address)
            val viewModel = initViewModelWithPaymentIntent(initPaymentIntent)
            val paymentStateFakeFlow: MutableStateFlow<Boolean> = MutableStateFlow(true)
            whenever(observePaymentStatus.observePaymentStates()).thenReturn(paymentStateFakeFlow)
            // act
            viewModel.onPayClicked()
            // assert
            Assert.assertEquals(expected, viewModel.state.value)
            verify(refreshPaymentIntentUseCase).refreshPaymentIntent(paymentIntentDomainEntity.id)
            verify(virtualTerminalHandler).executeVirtualTerminalPayment(paymentIntentDomainEntity.paymentToken, address)
        }

    private fun initViewModelWithPaymentIntent(initPaymentIntent: VirtualTerminalViewState): VirtualTerminalViewModel {
        val paymentIntentFakeFlow: MutableStateFlow<PaymentIntentResult?> =
            MutableStateFlow(null)
        val paymentStateFakeFlow: MutableStateFlow<Boolean> = MutableStateFlow(false)
        whenever(observePaymentIntent.observePaymentIntent()).thenReturn(paymentIntentFakeFlow)
        paymentIntentFakeFlow.tryEmit(
            PaymentIntentResult.Success(
                paymentIntentDomainEntity.copy(
                    collectionShippingAddressRequired = true
                )
            )
        )
        whenever(observePaymentStatus.observePaymentStates()).thenReturn(paymentStateFakeFlow)
        whenever(virtualTerminalViewEntityMapper.apply(any(), any())).thenReturn(
            initPaymentIntent
        )
        val viewModel = VirtualTerminalViewModel(
            observePaymentIntent,
            observePaymentStatus,
            updatePaymentStateUseCase,
            getSupportedCountriesUseCase,
            virtualTerminalValidator,
            virtualTerminalHandler,
            fullCardPaymentPayloadMapper,
            virtualTerminalViewEntityMapper,
            refreshPaymentIntentUseCase
        )
        return viewModel
    }
}

private object TestData {
    val paymentIntentDomainEntity = PaymentIntentDomainEntity(
        id = "id",
        paymentToken = "token",
        amount = AmountDomainEntity(
            10L, "100", "GBP"
        ),
        supportedCardsSchemes = listOf(CardsSchemes.AMEX),
        collectionBillingAddressRequired = true,
        collectionShippingAddressRequired = false,
        customerId = "customerId",
        orderId = "orderId"
    )
    val virtualTerminalViewState = VirtualTerminalViewState(
        isLoading = false,
        paymentDetailsSection = PaymentDetailsViewState(
            merchantName = "", totalAmount = "100", amountCurrency = "£", orderId = "orderId"
        ),
        shippingAddressSection = ShippingAddressViewState(
            isVisible = false, itemPoissonOffset = 0,
            name = InputFieldState(
                value = "", errorMessages = null, isError = false, isVisible = true
            ),
            addressLine1 = InputFieldState(
                value = "", errorMessages = null, isError = false, isVisible = true
            ),
            addressLine2 = InputFieldState(
                value = "", errorMessages = null, isError = false, isVisible = true
            ),
            city = InputFieldState(
                value = "", errorMessages = null, isError = false, isVisible = true
            ),
            postalCode = InputFieldState(
                value = "", errorMessages = null, isError = false, isVisible = true
            ),
            supportedCountriesList = listOf(
                SupportedCountriesViewEntity(
                    countryName = "EGP", countryCode = "EG", isPostalCodeEnabled = true
                ),
                SupportedCountriesViewEntity(
                    countryName = "EGP", countryCode = "EG", isPostalCodeEnabled = true
                )
            ),
            currentSelectedCountry = SupportedCountriesViewEntity(
                countryName = "EGP", countryCode = "EG", isPostalCodeEnabled = true
            ),
            deliveryNotes = InputFieldState(
                value = "", errorMessages = null, isError = false, isVisible = true
            ),
            isShippingSameAsBillingCheckBox = CheckBoxItem(
                messageText = R.string.dojo_ui_sdk_card_details_checkout_billing_same_as_shipping,
                isChecked = true,
                isVisible = true
            )
        ),
        billingAddressSection = BillingAddressViewState(
            isVisible = true, itemPoissonOffset = 50,
            addressLine1 = InputFieldState(
                value = "", errorMessages = null, isError = false, isVisible = true
            ),
            addressLine2 = InputFieldState(
                value = "", errorMessages = null, isError = false, isVisible = true
            ),
            city = InputFieldState(
                value = "", errorMessages = null, isError = false, isVisible = true
            ),
            postalCode = InputFieldState(
                value = "", errorMessages = null, isError = false, isVisible = true
            ),
            supportedCountriesList = listOf(
                SupportedCountriesViewEntity(
                    countryName = "EGP", countryCode = "EG", isPostalCodeEnabled = true
                ),
                SupportedCountriesViewEntity(
                    countryName = "EGP", countryCode = "EG", isPostalCodeEnabled = true
                )
            ),
            currentSelectedCountry = SupportedCountriesViewEntity(
                countryName = "EGP", countryCode = "EG", isPostalCodeEnabled = true
            )
        ),
        cardDetailsSection = CardDetailsViewState(
            isVisible = true, itemPoissonOffset = 700,
            emailInputField = InputFieldState(
                value = "", errorMessages = null, isError = false, isVisible = false
            ),
            cardHolderInputField = InputFieldState(
                value = "", errorMessages = null, isError = false, isVisible = true
            ),
            cardNumberInputField = InputFieldState(
                value = "", errorMessages = null, isError = false, isVisible = true
            ),
            cardExpireDateInputField = InputFieldState(
                value = "", errorMessages = null, isError = false, isVisible = true
            ),
            cvvInputFieldState = InputFieldState(
                value = "", errorMessages = null, isError = false, isVisible = true
            ),
            allowedPaymentMethodsIcons = emptyList()
        ),
        payButtonSection = PayButtonViewState(
            isEnabled = false, isLoading = false
        )
    )
}
