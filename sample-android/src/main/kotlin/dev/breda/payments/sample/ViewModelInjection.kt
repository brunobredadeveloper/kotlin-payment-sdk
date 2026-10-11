package dev.breda.payments.sample

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel

/** A composition root owns adapter construction; ViewModels receive abstractions. */
class InjectedViewModelFactory<T : ViewModel>(private val create: () -> T) :
    ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <V : ViewModel> create(modelClass: Class<V>): V = create() as V
}

@Composable
fun accountViewModel(): AccountSession {
    val context = LocalContext.current.applicationContext
    val factory =
        remember(context) {
            InjectedViewModelFactory {
                AccountSession(FirebaseAuthentication(context), PreferenceSessionStore(context))
            }
        }
    return viewModel(factory = factory)
}

@Composable
fun paymentViewModel(uid: String?): PaymentViewModel {
    val factory =
        remember(uid) {
            InjectedViewModelFactory {
                PaymentViewModel(PrepareExperiment(SimulatedClientFactory()))
            }
        }
    return viewModel(key = "lab-$uid", factory = factory)
}
