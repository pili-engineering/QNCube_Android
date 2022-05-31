package com.qncube.liveuikit

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.qncube.liveroomcore.QNLiveRoomClient
import com.qncube.uikitcore.KitContext
import kotlinx.android.synthetic.main.kit_fragment_cover.*

class CoverFragment : Fragment() {

    lateinit var mClient: QNLiveRoomClient
    private val mKitContext by lazy {
        object : KitContext {
            override var androidContext: Context = requireContext()
            override var fm: FragmentManager = childFragmentManager
            override var currentActivity: FragmentActivity = requireActivity()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.kit_fragment_cover, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        emptyInnerSlot.attach(
            QNLiveRoomUIKit.mViewSlotTable.mInnerCoverSlot,
            this,
            mKitContext,
            mClient
        )
        roomHostSlot.attach(
            QNLiveRoomUIKit.mViewSlotTable.mRoomHostSlot,
            this,
            mKitContext,
            mClient
        )
        onLineUserSlot.attach(
            QNLiveRoomUIKit.mViewSlotTable.mOnlineUserSlot,
            this,
            mKitContext,
            mClient
        )

        roomMemberSlot.attach(
            QNLiveRoomUIKit.mViewSlotTable.mRoomMemberCountSlot,
            this,
            mKitContext,
            mClient
        )

        roomIdSlot.attach(QNLiveRoomUIKit.mViewSlotTable.mRoomIdSlot, this, mKitContext, mClient)
        roomTimerSlot.attach(
            QNLiveRoomUIKit.mViewSlotTable.mRoomTimerSlot,
            this,
            mKitContext,
            mClient
        )
        inPutSlot.attach(QNLiveRoomUIKit.mViewSlotTable.mInputSlot, this, mKitContext, mClient)
        startPKSlot.attach(QNLiveRoomUIKit.mViewSlotTable.mStartPKSlot, this, mKitContext, mClient)


        bottomBarSlot.attach(
            QNLiveRoomUIKit.mViewSlotTable.mBottomFucBarSlot,
            this,
            mKitContext,
            mClient
        )

        pubchatSlot.attach(
            QNLiveRoomUIKit.mViewSlotTable.mPublicChatSlot,
            this,
            mKitContext,
            mClient
        )
        danmakuSlot.attach(
            QNLiveRoomUIKit.mViewSlotTable.mDanmakuTrackManagerSlot,
            this,
            mKitContext,
            mClient
        )

        pkCover.attach(QNLiveRoomUIKit.mViewSlotTable.mPKCoverSlot, this, mKitContext, mClient)

        emptyOutSlot.attach(
            QNLiveRoomUIKit.mViewSlotTable.mOuterCoverSlot,
            this,
            mKitContext,
            mClient
        )

    }
}