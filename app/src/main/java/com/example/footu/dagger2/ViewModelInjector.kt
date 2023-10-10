//
//import com.example.footu.dagger2.ViewModelScope
//import com.example.footu.dagger2.ApplicationComponent
//import com.example.footu.dagger2.NetworkModule
//import com.example.footu.ui.Order.OrderSViewModel
//import com.example.footu.ui.account.AccountViewModel
//import com.example.footu.ui.login.LoginViewModel
//import com.example.footu.ui.orderlist.OrderListViewModel
//import com.example.footu.ui.pay.PayConfirmViewModel
//import dagger.Component
//
//@Component(
//    modules = [NetworkModule::class],
//    dependencies = [ApplicationComponent::class],
//)
//@ViewModelScope
//interface ViewModelInjector {
//    fun inject(viewModel: LoginViewModel)
//    fun inject(viewModel: OrderSViewModel)
//    fun inject(viewModel: PayConfirmViewModel)
//    fun inject(viewModel: OrderListViewModel)
//    fun inject(viewModel: AccountViewModel)
//
//
//    @Component.Builder
//    interface Builder {
//        fun build(): ViewModelInjector
//        fun networkModule(networkModule: NetworkModule): Builder
//        fun applicationComponent(applicationComponent: ApplicationComponent): Builder
//    }
//}
