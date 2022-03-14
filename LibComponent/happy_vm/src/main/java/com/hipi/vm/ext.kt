package com.hipi.vm

import android.app.Application
import android.os.Bundle
import android.util.Log
import androidx.annotation.MainThread
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.createViewModelLazy
import androidx.lifecycle.*
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
import java.lang.reflect.InvocationTargetException


class FragmentVmFac(
    private val application: Application,
    private val bundle: Bundle?,
    private val f: Fragment
) : AndroidViewModelFactory(application) {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return try {
            val constructor1 = modelClass.getConstructor(
                Application::class.java,
                Bundle::class.java
            )
            val vm: T = constructor1.newInstance(application, bundle)

            if (vm is BaseViewModel) {
                vm.finishedActivityCall = { f.activity?.finish() }
                vm.getFragmentManagrCall = { f.childFragmentManager }
                if (f is LoadingObserverView) {
                    vm.showLoadingCall = {
                        f.showLoading(it)
                    }
                }
                f.lifecycle.addObserver(vm)
            }
            vm
        } catch (e: NoSuchMethodException) {
            val vmodel = super.create(modelClass)
            if (vmodel is BaseViewModel) {
                vmodel.finishedActivityCall = { f.activity?.finish() }
                vmodel.getFragmentManagrCall = { f.childFragmentManager }
                if (f is LoadingObserverView) {
                    vmodel.showLoadingCall = {
                        f.showLoading(it)
                    }
                }
                f.lifecycle.addObserver(vmodel)
            }

            vmodel as T
        } catch (e: IllegalAccessException) {
            throw RuntimeException("Cannot create an instance of $modelClass", e)
        } catch (e: InstantiationException) {
            throw RuntimeException("Cannot create an instance of $modelClass", e)
        } catch (e: InvocationTargetException) {
            throw RuntimeException("Cannot create an instance of $modelClass", e)
        }
    }
}


class ActivityVmFac(
    private val application: Application,
    private val bundle: Bundle?,
    private val act: FragmentActivity
) : AndroidViewModelFactory(application) {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return try {
            val constructor1 = modelClass.getConstructor(
                Application::class.java,
                Bundle::class.java
            )
            val vm: T =  constructor1.newInstance(application, bundle)

            if (vm is BaseViewModel) {
                vm.finishedActivityCall = { act.finish() }
                vm.getFragmentManagrCall = { act.supportFragmentManager }
                if (act is LoadingObserverView) {
                    vm.showLoadingCall = {
                        act.showLoading(it)
                    }
                }
                act.lifecycle.addObserver(vm)
            }
            vm

        } catch (e: NoSuchMethodException) {
           val vm2 = super.create(modelClass)

            if (vm2 is BaseViewModel) {
                vm2.finishedActivityCall = { act.finish() }
                vm2.getFragmentManagrCall = { act.supportFragmentManager }
                if (act is LoadingObserverView) {
                    vm2.showLoadingCall = {
                        act.showLoading(it)
                    }
                }
                act.lifecycle.addObserver(vm2)
            }
            vm2
        } catch (e: IllegalAccessException) {
            throw RuntimeException("Cannot create an instance of $modelClass", e)
        } catch (e: InstantiationException) {
            throw RuntimeException("Cannot create an instance of $modelClass", e)
        } catch (e: InvocationTargetException) {
            throw RuntimeException("Cannot create an instance of $modelClass", e)
        }
    }
}

//创建 fragment vm
@MainThread
inline fun <reified VM : BaseViewModel> Fragment.createVm(
    noinline ownerProducer: () -> ViewModelStoreOwner = { this },
    noinline factoryProducer: (() -> ViewModelProvider.Factory)? = {
        FragmentVmFac(
            activity!!.application,
            arguments,
            this
        )
    }
): Lazy<VM> {
    val vm = createViewModelLazy(VM::class, { ownerProducer().viewModelStore }, factoryProducer);
    lifecycleScope.launchWhenCreated {
        Log.d("createVm", "vm.value.mData==null  ${vm.value.javaClass} ${vm.value.mData == null}")
    }
    return vm
}

//创建activity vm
inline fun <reified VM : BaseViewModel> FragmentActivity.createVm(
    noinline factoryProducer: (() -> ViewModelProvider.Factory)? = null
): Lazy<VM> {
    val factoryPromise = factoryProducer ?: {
        ActivityVmFac(application, intent.extras, this);
    }
    val vm = ViewModelLazy(VM::class, { viewModelStore }, factoryPromise)
    val act = this
    lifecycleScope.launchWhenCreated {
        Log.d("createVm", "vm.value.mData==null  ${vm.value.javaClass} ${vm.value.mData == null}")
    }

    return vm
}

//懒加载创建
inline fun <reified VM : BaseViewModel> FragmentActivity.lazyVm(
    noinline factoryProducer: (() -> ViewModelProvider.Factory)? = null
): Lazy<VM> {
    val factoryPromise = factoryProducer ?: {
        ActivityVmFac(application, intent.extras, this);
    }
    val vm = ViewModelLazy(VM::class, { viewModelStore }, factoryPromise)
    return vm
}

@MainThread
inline fun <reified VM : BaseViewModel> Fragment.lazyVm(
    noinline ownerProducer: () -> ViewModelStoreOwner = { this },
    noinline factoryProducer: (() -> ViewModelProvider.Factory)? = {
        FragmentVmFac(
            activity!!.application,
            arguments,
            this
        )
    }
): Lazy<VM> {
    val vm = createViewModelLazy(VM::class, { ownerProducer().viewModelStore }, factoryProducer);

    return vm
}


@MainThread
inline fun <reified VM : BaseViewModel> Fragment.activityVm(
    noinline factoryProducer: (() -> ViewModelProvider.Factory)? = null
) = createViewModelLazy(VM::class, { requireActivity().viewModelStore },
    factoryProducer ?: {

        val act = requireActivity()
        ActivityVmFac(act.application, act.intent.extras, act);
    }
)

