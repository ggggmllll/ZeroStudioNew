package com.catpuppyapp.puppygit.utils.state

/** 所有remember统一调用此处的函数获取，这样如果日后替换实现，方便修改 */
@Deprecated(
    "it make android compose new version after 20241006 compile kotlin file incorrect, e.g. a state should false, but become true"
)
object StateUtil {
  //    @Composable
  //    fun<T> getRememberState(initValue:T): MutableState<T> {
  //        return remember { mutableStateOf(initValue) }
  //    }

  //    @Composable
  //    fun getRememberIntState(initValue:Int): MutableIntState {
  //        return remember { mutableIntStateOf(initValue) }
  //    }

  // 不用mutableStateOf() 包装值，适用于focusRequester 之类的东西
  //    @Composable
  //    fun<T> getRememberStateRawValue(initValue:T): T {
  //        return remember { initValue }
  //    }

  //    @Composable
  //    fun<T> getRememberSaveableState(initValue:T): MutableState<T> {
  //        return rememberSaveable { mutableStateOf(initValue) }
  //    }
  //    @Composable
  //    fun<T> getRememberSaveableState(initValue:()->T): MutableState<T> {
  //        return rememberSaveable { mutableStateOf(initValue()) }
  ////    }
  //    @Composable
  //    fun getRememberSaveableLongState(initValue:Long): MutableLongState {
  //        return rememberSaveable { mutableLongStateOf(initValue) }
  //    }

  //    @Composable
  //    fun<T:Any> getRememberSaveableRawState(initValue:T): T {
  //        return rememberSaveable { initValue }
  //    }
  //
  //    @Composable
  //    fun<T> getCustomSaveableState(keyTag:String, keyName:String,
  // initValue:T):CustomStateSaveable<T> {
  //        return mutableCustomStateOf(keyTag, keyName, initValue)
  //    }
  //
  //    @Composable
  //    fun<T> getCustomSaveableStateList(keyTag:String, keyName:String,
  // initValue:List<T>):CustomStateListSaveable<T> {
  //        return mutableCustomStateListOf(keyTag, keyName, initValue)
  //    }

  //    @Composable
  //    fun<K,V> getCustomSaveableStateMap(keyTag:String, keyName:String,
  // initValue:Map<K,V>):CustomStateMapSaveable<K,V> {
  //        return mutableCustomStateMapOf(keyTag, keyName, initValue)
  //    }

  //    @Composable
  //    fun<T> getCustomSaveableState(keyTag:String, keyName:String,
  // getInitValue:()->T):CustomStateSaveable<T> {
  //        return mutableCustomStateOf(keyTag, keyName, getInitValue())
  //    }

  //    @Composable
  //    fun<T> getCustomSaveableStateList(keyTag:String, keyName:String,
  // getInitValue:()->List<T>):CustomStateListSaveable<T> {
  //        return mutableCustomStateListOf(keyTag, keyName, getInitValue())
  //    }
  //
  //    @Composable
  //    fun<K,V> getCustomSaveableStateMap(keyTag:String, keyName:String,
  // getInitValue:()->Map<K,V>):CustomStateMapSaveable<K,V> {
  //        return mutableCustomStateMapOf(keyTag, keyName, getInitValue())
  //    }

  //    @Composable
  //    fun getRememberSaveableIntState(initValue:Int):MutableIntState {
  //        return rememberSaveable{mutableIntStateOf(initValue)}
  //    }
  //    @Composable
  //    fun getRememberSaveableIntState(initValue:()->Int):MutableIntState {
  //        return rememberSaveable{mutableIntStateOf(initValue())}
  //    }

  //    @OptIn(ExperimentalMaterial3Api::class)
  //    @Composable
  //    fun getRememberModalBottomSheetState(): SheetState {
  //        // saveable actually
  //        //skipPartiallyExpanded=true，作用是默认直接展开菜单，不然菜单项多时还要向上滑一下才能展开，不适合我的使用场景
  //        return rememberModalBottomSheetState(skipPartiallyExpanded =
  // MyStyleKt.BottomSheet.skipPartiallyExpanded)
  //    }

  //    @Composable
  //    fun getRememberLazyListState(): LazyListState {
  //        // lazy list state is saveable
  //        return rememberLazyListState()
  //    }

  //    @Composable
  //    fun getRememberScrollState(): ScrollState {
  //        // saveable actually
  //        return rememberScrollState()
  //    }

  //    private fun genCustomStateSaveableKeyPrefix(keyTag:String, keyDesc:String):String {
  //        return keyTag+"_"+keyDesc
  //    }

}
