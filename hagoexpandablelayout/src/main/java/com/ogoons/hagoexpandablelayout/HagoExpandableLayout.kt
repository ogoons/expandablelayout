package com.ogoons.hagoexpandablelayout

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import android.widget.LinearLayout

/**
 * Created by charles on 2018. 4. 3..
 */
class HagoExpandableLayout(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {

    companion object {
        const val HORIZONTAL = 0
        const val VERTICAL = 1

        private const val DEF_DURATION = 500
        private const val DEF_PARALLAX = 1f
        private const val DEF_EXPANDED = false
        private const val DEF_ORIENTATION = VERTICAL

        private const val EXPANDED = 1f
        private const val COLLAPSED = 0f

        private const val ARG_SUPER_STATE = "super_state"
        private const val ARG_EXPANSION = "expansion"
    }

    enum class State {
        COLLAPSING,
        COLLAPSED,
        EXPANDING,
        EXPANDED
    }

    var duration: Int? = null

    var parallax: Float? = null

    private var expansion: Float? = null

    var orientation: Int? = null

    private var state: State? = null

    var interpolator = DecelerateInterpolator()

    private var animator: ValueAnimator? = null

    var listener: OnExpansionChangeListener? = null

    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.HagoExpandableLayout)
        duration = typedArray.getInt(R.styleable.HagoExpandableLayout_duration, DEF_DURATION)
        parallax = typedArray.getFloat(R.styleable.HagoExpandableLayout_parallax, DEF_PARALLAX)
        expansion = if (typedArray.getBoolean(R.styleable.HagoExpandableLayout_expanded, DEF_EXPANDED)) EXPANDED else COLLAPSED
        orientation = typedArray.getInt(R.styleable.HagoExpandableLayout_android_orientation, DEF_ORIENTATION)
        typedArray.recycle()

        state = if (isExpanded()) State.EXPANDED else State.COLLAPSED
        setParallax(parallax!!)
    }

    override fun onSaveInstanceState(): Parcelable {
        expansion = if (isExpanded()) EXPANDED else COLLAPSED
        return Bundle().apply {
            putParcelable(ARG_SUPER_STATE, super.onSaveInstanceState())
            putFloat(ARG_EXPANSION, expansion!!)
        }
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        var newState = state
        if (state is Bundle) {
            newState = state.getParcelable(ARG_SUPER_STATE)
            expansion = state.getFloat(ARG_EXPANSION)
        }
        super.onRestoreInstanceState(newState)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val width = measuredWidth
        val height = measuredHeight

        val size = if (orientation == LinearLayout.HORIZONTAL) width else height

        visibility = if (expansion == 0f && size == 0) View.GONE else View.VISIBLE

        val expansionDelta = size - Math.round(size * expansion!!)
        if (parallax!! > 0) {
            val parallaxDelta = expansionDelta * parallax!!
            for (i in 0 until childCount) {
                val child = getChildAt(i)
                if (orientation == HORIZONTAL) {
                    var direction = -1
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1 && layoutDirection == View.LAYOUT_DIRECTION_RTL) {
                        direction = 1
                    }
                    child.translationX = direction * parallaxDelta
                } else {
                    child.translationY = -parallaxDelta
                }
            }
        }

        if (orientation == HORIZONTAL) {
            setMeasuredDimension(width - expansionDelta, height)
        } else {
            setMeasuredDimension(width, height - expansionDelta)
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        if (animator != null) {
            animator!!.cancel()
        }
        super.onConfigurationChanged(newConfig)
    }

    fun isExpanded(): Boolean {
        return state == State.EXPANDING || state == State.EXPANDED
    }

    fun expand(isAnimate: Boolean = true) {
        setExpand(true, isAnimate)
    }

    fun collapse(isAnimate: Boolean = true) {
        setExpand(false, isAnimate)
    }

    fun toggle(isAnimate: Boolean = true) {
        if (isExpanded()) {
            collapse(isAnimate)
        } else {
            expand(isAnimate)
        }
    }

    fun setExpand(isExpanded: Boolean, isAnimate: Boolean) {
        if (isExpanded == isExpanded()) return
        val destExpansion = if (isExpanded) EXPANDED else COLLAPSED
        if (isAnimate) {
            animateExpansion(destExpansion)
        } else {
            setExpansion(destExpansion)
        }
    }

    /**
     * 뷰의 보이기 / 감추기
     */
    fun setExpansion(expansion: Float) {
        if (this.expansion == expansion) return

        val delta = expansion - this.expansion!!

        if (expansion == COLLAPSED)
            state = State.COLLAPSED
        else if (expansion == EXPANDED)
            state = State.EXPANDED
        else if (delta < 0)
            state = State.COLLAPSING
        else if (delta > 0)
            state = State.EXPANDING

        visibility = if (state == State.COLLAPSED) View.GONE else View.VISIBLE

        this.expansion = expansion

        requestLayout()

        listener?.onExpansionChanged(expansion, state!!)
    }

    private fun animateExpansion(destExpansion: Float) {
        animator?.cancel()
        animator = null

        animator = ValueAnimator.ofFloat(expansion!!, destExpansion)
        animator!!.interpolator = interpolator
        animator!!.duration = duration!!.toLong()

        // 확장/축소의 변화가 트리거되는 리스너의 콜백
        animator!!.addUpdateListener { valueAnimator ->
            setExpansion(valueAnimator.animatedValue as Float)
        }

        // state를 설정할 용도의 리스너 장착
        animator!!.addListener(ExpansionAnimationListener(destExpansion))
        animator!!.start()
    }

    fun setOrientation(orientation: Int): Boolean {
        if (orientation < HORIZONTAL || orientation > VERTICAL) return false
        this.orientation = orientation
        return true
    }

    fun setParallax(parallax: Float) {
        this.parallax = Math.min(EXPANDED, Math.max(COLLAPSED, parallax))
    }

    interface OnExpansionChangeListener {
        fun onExpansionChanged(expansion: Float, state: State)
    }

    inner class ExpansionAnimationListener(
            private val destExpansion: Float
    ) : Animator.AnimatorListener {
        private var canceled = false

        override fun onAnimationRepeat(p0: Animator?) {
        }

        override fun onAnimationEnd(p0: Animator?) {
            if (!canceled) {
                state = if (destExpansion == COLLAPSED) State.COLLAPSED else State.EXPANDED
            }
        }

        override fun onAnimationCancel(p0: Animator?) {
            canceled = true
        }

        override fun onAnimationStart(p0: Animator?) {
            state = if (destExpansion == COLLAPSED) State.COLLAPSING else State.EXPANDING
        }
    }

}