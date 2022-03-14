package com.niucube.module.ktv

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.Glide
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.hapi.ut.ViewUtil
import com.hipi.vm.activityVm
import com.hipi.vm.backGround
import com.niucube.comproom.RoomManager
import com.niucube.module.ktv.mode.Song
import com.niucube.module.ktv.playerlist.KTVPlaylistsManager
import com.qiniu.bzcomp.user.UserInfoManager
import com.qiniu.comp.network.RetrofitManager
import com.qiniudemo.baseapp.BaseDialogFragment
import com.qiniudemo.baseapp.CommonRecyclerFragment
import com.qiniudemo.baseapp.ext.asToast
import com.qiniudemo.baseapp.widget.CommonPagerAdapter
import kotlinx.android.synthetic.main.dialog_song_list.*
import kotlinx.android.synthetic.main.item_songs.view.*
import net.lucode.hackware.magicindicator.ViewPagerHelper
import net.lucode.hackware.magicindicator.buildins.UIUtil
import net.lucode.hackware.magicindicator.buildins.commonnavigator.indicators.LinePagerIndicator

import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerIndicator

import net.lucode.hackware.magicindicator.buildins.commonnavigator.titles.ColorTransitionPagerTitleView

import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerTitleView

import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.CommonNavigatorAdapter

import net.lucode.hackware.magicindicator.buildins.commonnavigator.CommonNavigator


class SongsListDialog : BaseDialogFragment() {

    init {
        applyGravityStyle(Gravity.BOTTOM)
    }

    private val ktvRoomVm by activityVm<KTVRoomVm>()

    private val tittles = listOf<String>("点歌", "已点")
    private val fragments by lazy {
        listOf<SongsFragment>(
            AllSongsFragment(),
            SelectSongsFragment()
        )
    }

    override fun initViewData() {

        vpSongs.adapter = CommonPagerAdapter(fragments, childFragmentManager, tittles)
        val commonNavigator = CommonNavigator(requireContext())
        commonNavigator.adapter = object : CommonNavigatorAdapter() {
            override fun getCount(): Int {
                return tittles.size
            }

            override fun getTitleView(context: Context, index: Int): IPagerTitleView {
                val colorTransitionPagerTitleView = ColorTransitionPagerTitleView(context)
                colorTransitionPagerTitleView.normalColor = Color.parseColor("#666666")
                colorTransitionPagerTitleView.selectedColor = Color.parseColor("#EF4149")
                colorTransitionPagerTitleView.setText(tittles.get(index))
                colorTransitionPagerTitleView.setOnClickListener { vpSongs.setCurrentItem(index) }
                return colorTransitionPagerTitleView
            }

            override fun getIndicator(context: Context?): IPagerIndicator? {
                val linePagerIndicator = LinePagerIndicator(context)
                linePagerIndicator.mode = LinePagerIndicator.MODE_EXACTLY
                linePagerIndicator.lineWidth = UIUtil.dip2px(context, 10.0).toFloat()
                linePagerIndicator.setColors(Color.parseColor("#EF4149"))
                return linePagerIndicator
            }

        }
        magic_indicator.navigator = commonNavigator
        val titleContainer = commonNavigator.titleContainer // must after setNavigator

        titleContainer.showDividers = LinearLayout.SHOW_DIVIDER_MIDDLE
        titleContainer.dividerDrawable = object : ColorDrawable() {
            override fun getIntrinsicWidth(): Int {
                return UIUtil.dip2px(requireContext(), 15.0)
            }
        }
        ViewPagerHelper.bind(magic_indicator, vpSongs);
        vpSongs.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {

            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
                fragments[position].refresh()
            }

            override fun onPageScrollStateChanged(state: Int) {}

        })
    }

    override fun getViewLayoutId(): Int {
        return R.layout.dialog_song_list
    }


    abstract class SongsFragment : CommonRecyclerFragment<Song>() {

        fun refresh() {
            mSmartRecycler.startRefresh()
        }
    }

    class AllSongsFragment : SongsFragment() {
        private val ktvRoomVm by activityVm<KTVRoomVm>()
        override val adapter: BaseQuickAdapter<Song, *> by lazy {
            SongAdapter(0, ktvRoomVm.mKTVPlaylistsManager).apply {
                opCall = { position ->
                    backGround {
                        showLoading(true)
                        doWork {
                            ktvRoomVm.mKTVPlaylistsManager.addToPlaylists(data[position])
                            remove(position)
                            ktvRoomVm.mKTVPlaylistsManager.fetchPlaylists()
                        }
                        catchError {
                            it.printStackTrace()
                            it.message?.asToast()
                        }
                        onFinally {
                            showLoading(false)
                        }
                    }
                }
            }
        }

        override val layoutManager: RecyclerView.LayoutManager by lazy {
            LinearLayoutManager(
                requireContext()
            )
        }

        /**
         * 刷新回调
         */
        override val fetcherFuc: (page: Int) -> Unit = {
            backGround {
                doWork {
                    val list = RetrofitManager.create(KTVService::class.java)
                        .songList(10, it + 1, RoomManager.mCurrentRoom?.provideRoomId() ?: "")
                    mSmartRecycler.onFetchDataFinish(list.list, true, list.isEndPage)
                }
                catchError {
                    mSmartRecycler.onFetchDataError()
                }
            }
        }
    }

    class SelectSongsFragment : SongsFragment() {

        private val ktvRoomVm by activityVm<KTVRoomVm>()
        override val adapter: BaseQuickAdapter<Song, *> by lazy {
            SongAdapter(
                1,
                ktvRoomVm.mKTVPlaylistsManager
            ).apply {
                opCall = { position ->
                    backGround {
                        showLoading(true)
                        doWork {
                            ktvRoomVm.mKTVPlaylistsManager.removePlaylist(data[position])
                            remove(position)
                            ktvRoomVm.mKTVPlaylistsManager.fetchPlaylists()
                        }
                        catchError {
                            it.printStackTrace()
                            it.message?.asToast()
                        }
                        onFinally {
                            showLoading(false)
                        }
                    }

                }
            }
        }
        override val layoutManager: RecyclerView.LayoutManager by lazy {
            LinearLayoutManager(
                requireContext()
            )
        }

        /**
         * 刷新回调
         */
        override val fetcherFuc: (page: Int) -> Unit = {
            backGround {
                doWork {
                    val songs = ktvRoomVm.mKTVPlaylistsManager.fetchPlaylists()
                    mSmartRecycler.onFetchDataFinish(songs, true, true)
                }
                catchError {
                    it.printStackTrace()
                    it.message?.asToast()
                    mSmartRecycler.onFetchDataError()

                }
            }
        }
    }

    class SongAdapter(val type: Int, val mKTVPlaylistsManager: KTVPlaylistsManager) :
        BaseQuickAdapter<Song, BaseViewHolder>(R.layout.item_songs, ArrayList<Song>()) {

        var opCall: (index: Int) -> Unit = {
        }

        override fun convert(helper: BaseViewHolder, item: Song) {
            helper.itemView.tvSongName.text = item.name

            Glide.with(mContext)
                .load(item.image)
                .placeholder(R.drawable.pic_empty)
                .into(helper.itemView.tvSongCover)
            helper.itemView.tvSinger.text = item.author
            if (type == 1) {
                helper.itemView.tvOp.text = "移除"
                helper.itemView.isClickable = true
                helper.itemView.tvOp.setOnClickListener {
                    opCall.invoke(data.indexOf(item))
                }
            } else {
                var selected = false
                mKTVPlaylistsManager.getPlaylists().forEach {
                    if (it.getMusicId() == item.getMusicId() && (it as Song).demander == UserInfoManager.getUserId()) {
                        selected = true
                        return@forEach
                    }
                }
                if (selected) {
                    helper.itemView.tvOp.text = "已点"
                    helper.itemView.isClickable = false
                    helper.itemView.tvOp.setOnClickListener {
                    }
                } else {
                    helper.itemView.tvOp.text = "点歌"
                    helper.itemView.isClickable = true
                    helper.itemView.tvOp.setOnClickListener {
                        opCall.invoke(data.indexOf(item))
                    }
                }
            }

        }
    }
}