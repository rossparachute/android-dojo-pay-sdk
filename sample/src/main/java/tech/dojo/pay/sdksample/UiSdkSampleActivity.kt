package tech.dojo.pay.sdksample

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import tech.dojo.pay.sdk.DojoPaymentResult
import tech.dojo.pay.sdk.card.entities.DojoGPayConfig
import tech.dojo.pay.sdksample.customer.CustomerGenerator
import tech.dojo.pay.sdksample.databinding.ActivityUiSdkSampleBinding
import tech.dojo.pay.sdksample.token.PaymentIDGenerator
import tech.dojo.pay.uisdk.DojoSDKDropInUI
import tech.dojo.pay.uisdk.entities.DojoPaymentFlowParams
import tech.dojo.pay.uisdk.entities.DojoThemeSettings

class UiSdkSampleActivity : AppCompatActivity() {
    private lateinit var uiSdkSampleBinding: ActivityUiSdkSampleBinding
    private val dojoPayUI =
        DojoSDKDropInUI.createUIPaymentHandler(this) { result -> showResult(result) }

    var secret = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        uiSdkSampleBinding = ActivityUiSdkSampleBinding.inflate(layoutInflater)
        setContentView(uiSdkSampleBinding.root)
        setTokenListener()
        setCustomerCreationListener()

        uiSdkSampleBinding.startPaymentFlow.setOnClickListener {
            DojoSDKDropInUI.dojoThemeSettings = DojoThemeSettings(forceLightMode = false)
            dojoPayUI.startPaymentFlow(
                DojoPaymentFlowParams(uiSdkSampleBinding.token.text.toString())
            )
        }
        uiSdkSampleBinding.startPaymentFlowWithTheme.setOnClickListener {
            DojoSDKDropInUI.dojoThemeSettings = DojoThemeSettings(forceLightMode = true)
            dojoPayUI.startPaymentFlow(
                DojoPaymentFlowParams(
                    uiSdkSampleBinding.token.text.toString(),
                    secret,
                    GPayConfig = DojoGPayConfig(
                        merchantName = "Dojo Cafe (Paymentsense)",
                        merchantId = "BCR2DN6T57R5ZI34",
                        gatewayMerchantId = "119784244252745"
                    ),
                    isVirtualTerminalPayment = true
                )
            )
        }
    }
//
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        val result = DojoSDKDropInUI.parseUIPaymentFlowResult(requestCode, resultCode, data)
//        if (result!= null ) showResult(result)
//    }

    private fun setTokenListener() {
        uiSdkSampleBinding.checkboxSandbox.setOnCheckedChangeListener { _, isChecked ->
            uiSdkSampleBinding.btnGenerateToken.visibility =
                if (isChecked) View.VISIBLE else View.GONE
            displayToken("")
        }

        uiSdkSampleBinding.btnGenerateToken.setOnClickListener {
            lifecycleScope.launch {
                showLoading()
                try {
                    displayToken(PaymentIDGenerator.generatePaymentId(uiSdkSampleBinding.userId.text.toString()).id)
                } catch (e: Throwable) {
                    showTokenError(e)
                } finally {
                    hideLoading()
                }
            }
        }
    }

    private fun setCustomerCreationListener() {
        uiSdkSampleBinding.btnGenerateCustomerID.setOnClickListener {
            uiSdkSampleBinding.userId.setText("")
            lifecycleScope.launch {
                showLoading()
                try {
                    val id = CustomerGenerator.generateCustomerId().id
                    secret = CustomerGenerator.getCustomerSecrete(id).secret
                    displayCustomerSecrete(id)
                } catch (e: Throwable) {
                    showTokenError(e)
                } finally {
                    hideLoading()
                }
            }
        }
    }

    private fun displayToken(token: String) {
        uiSdkSampleBinding.token.setText(token)
        uiSdkSampleBinding.token.visibility = View.VISIBLE
    }

    private fun displayCustomerSecrete(id: String) {
        uiSdkSampleBinding.userId.setText(id)
    }

    private fun showLoading() {
        uiSdkSampleBinding.viewProgress.visibility = View.VISIBLE
    }

    private fun hideLoading() {
        uiSdkSampleBinding.viewProgress.visibility = View.GONE
    }

    private fun showTokenError(e: Throwable) {
        uiSdkSampleBinding.viewProgress.visibility = View.GONE
        uiSdkSampleBinding.token.setText(e.message)
    }

    private fun showResult(result: DojoPaymentResult) {
        showDialog(
            title = "Payment result",
            message = "${result.name} (${result.code})"
        )
        displayToken("")
        displayCustomerSecrete("")
        secret = ""
    }

    private fun showDialog(title: String, message: String) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .create()
            .show()
    }
}
