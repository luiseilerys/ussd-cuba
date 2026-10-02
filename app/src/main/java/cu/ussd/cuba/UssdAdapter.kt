package cu.ussd.cuba

import android.graphics.Typeface
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import cu.ussd.cuba.databinding.ItemUssdBinding

class UssdAdapter(
    private val onClick: (UssdCode) -> Unit,
    private val onLongClick: (UssdCode) -> Unit,
    private val onFavoriteClick: (UssdCode) -> Unit,
    private val isFavorite: (String) -> Boolean,
    private val styleProvider: () -> ThemeHelper.UiStyle = {
        ThemeHelper.uiStyle("clasico")
    }
) : ListAdapter<UssdCode, UssdAdapter.ViewHolder>(DiffCallback) {

    object DiffCallback : DiffUtil.ItemCallback<UssdCode>() {
        override fun areItemsTheSame(old: UssdCode, new: UssdCode) = old.id == new.id
        override fun areContentsTheSame(old: UssdCode, new: UssdCode) = old == new
    }

    class ViewHolder(val binding: ItemUssdBinding) : RecyclerView.ViewHolder(binding.root)

    fun forceRestyle() {
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemUssdBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        val b = holder.binding
        val ctx = b.root.context
        val style = styleProvider()
        val density = ctx.resources.displayMetrics.density

        fun dp(v: Int) = (v * density).toInt()
        fun dpF(v: Float) = v * density

        val codeText = item.code.replace(Regex("\\{[^}]+\\}"), "…")
        val card = b.root as MaterialCardView

        card.radius = dpF(style.cornerRadiusDp)
        card.cardElevation = dpF(style.cardElevationDp)
        card.useCompatPadding = style.cardElevationDp > 0f
        if (style.strokeWidthDp > 0f) {
            card.strokeWidth = dpF(style.strokeWidthDp).toInt().coerceAtLeast(1)
            val tv = TypedValue()
            if (ctx.theme.resolveAttribute(com.google.android.material.R.attr.colorOutlineVariant, tv, true)) {
                card.strokeColor = tv.data
            }
        } else {
            card.strokeWidth = 0
        }

        val bgAttr = if (style.layoutMode == "MINIMAL")
            com.google.android.material.R.attr.colorSurface
        else
            com.google.android.material.R.attr.colorSurfaceContainer
        val tvBg = TypedValue()
        if (ctx.theme.resolveAttribute(bgAttr, tvBg, true)) {
            card.setCardBackgroundColor(tvBg.data)
        }

        val lp = card.layoutParams
        if (lp is ViewGroup.MarginLayoutParams) {
            val m = dp(style.itemMarginVDp)
            lp.topMargin = m
            lp.bottomMargin = m
            card.layoutParams = lp
        }
        card.minimumHeight = if (style.minItemHeightDp > 0) dp(style.minItemHeightDp) else 0

        b.accentBar.isVisible = false
        b.accentTop.isVisible = false
        b.tvCodeBanner.isVisible = false
        b.tvCodeSide.isVisible = false
        b.tvCode.isVisible = false
        b.tvCodePlain.isVisible = false
        b.tvDescription.isVisible = false

        b.tvTitle.text = item.title
        b.tvTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, style.titleSp)
        b.tvDescription.text = item.description
        b.tvDescription.setTextSize(TypedValue.COMPLEX_UNIT_SP, style.descSp)
        b.tvCode.text = codeText
        b.tvCode.setTextSize(TypedValue.COMPLEX_UNIT_SP, style.codeSp)
        b.tvCodeSide.text = codeText
        b.tvCodeSide.setTextSize(TypedValue.COMPLEX_UNIT_SP, style.codeSp)
        b.tvCodePlain.text = codeText
        b.tvCodePlain.setTextSize(TypedValue.COMPLEX_UNIT_SP, style.codeSp)
        b.tvCodeBanner.text = codeText
        b.tvCodeBanner.setTextSize(TypedValue.COMPLEX_UNIT_SP, style.codeSp)

        val favSize = dp(style.favButtonDp)
        b.btnFavorite.updateLayoutParams {
            width = favSize
            height = favSize
        }
        val fav = isFavorite(item.id)
        b.btnFavorite.setImageResource(
            if (fav) R.drawable.ic_star_filled else R.drawable.ic_star_outline
        )
        b.btnFavorite.clearColorFilter()

        val pad = dp(style.itemPaddingDp)

        when (style.layoutMode) {
            "MINIMAL" -> {
                b.bodyRow.orientation = LinearLayout.HORIZONTAL
                b.bodyRow.gravity = Gravity.CENTER_VERTICAL
                b.bodyRow.updatePadding(pad, pad, dp(4), pad)
                b.tvTitle.maxLines = 1
                b.tvCodePlain.isVisible = true
                b.tvCodePlain.typeface = Typeface.MONOSPACE
            }
            "CODE_LEFT" -> {
                b.accentTop.isVisible = style.showAccentTop
                b.bodyRow.orientation = LinearLayout.HORIZONTAL
                b.bodyRow.gravity = Gravity.CENTER_VERTICAL
                b.bodyRow.updatePadding(pad, pad, dp(4), pad)
                b.tvCodeSide.isVisible = true
                b.tvCodeSide.updateLayoutParams { width = dp(style.codeSideWidthDp) }
                b.tvCodeSide.setBackgroundResource(R.drawable.bg_code_chip)
                b.tvDescription.isVisible = style.showDescription && item.description.isNotBlank()
                b.tvTitle.maxLines = 2
            }
            "FLAT_LINE" -> {
                b.bodyRow.orientation = LinearLayout.HORIZONTAL
                b.bodyRow.gravity = Gravity.CENTER_VERTICAL
                b.bodyRow.updatePadding(pad, dp(4), dp(2), dp(4))
                b.tvCodeSide.isVisible = true
                b.tvCodeSide.updateLayoutParams { width = ViewGroup.LayoutParams.WRAP_CONTENT }
                b.tvCodeSide.setBackgroundResource(0)
                b.tvCodeSide.setPadding(0, 0, dp(8), 0)
                b.tvCodeSide.typeface = Typeface.MONOSPACE
                b.tvTitle.maxLines = 1
            }
            "STACK" -> {
                b.bodyRow.orientation = LinearLayout.VERTICAL
                b.bodyRow.gravity = Gravity.START
                b.bodyRow.updatePadding(pad, pad, pad, pad)
                b.tvTitle.maxLines = 2
                b.tvCode.isVisible = true
                b.tvCode.setTextSize(TypedValue.COMPLEX_UNIT_SP, style.codeSp)
                b.tvCode.updateLayoutParams { width = ViewGroup.LayoutParams.MATCH_PARENT }
                b.tvCode.gravity = Gravity.CENTER
                b.tvCode.setBackgroundResource(R.drawable.bg_code_chip)
                b.tvDescription.isVisible = style.showDescription && item.description.isNotBlank()
            }
            "BANNER" -> {
                b.tvCodeBanner.isVisible = true
                b.bodyRow.orientation = LinearLayout.HORIZONTAL
                b.bodyRow.gravity = Gravity.CENTER_VERTICAL
                b.bodyRow.updatePadding(dp(14), dp(12), dp(4), dp(14))
                b.tvTitle.maxLines = 2
                b.tvDescription.isVisible = style.showDescription && item.description.isNotBlank()
            }
            else -> {
                b.bodyRow.orientation = LinearLayout.HORIZONTAL
                b.bodyRow.gravity = Gravity.CENTER_VERTICAL
                b.bodyRow.updatePadding(pad, pad, dp(4), pad)
                b.accentBar.isVisible = style.showAccentBar
                b.tvTitle.maxLines = 1
                b.tvDescription.isVisible = style.showDescription && item.description.isNotBlank()
                b.tvCode.isVisible = style.showCodeChip
                b.tvCode.updateLayoutParams { width = ViewGroup.LayoutParams.WRAP_CONTENT }
                b.tvCode.gravity = Gravity.START
                b.tvCode.setBackgroundResource(R.drawable.bg_code_chip)
            }
        }

        b.root.setOnClickListener { onClick(item) }
        b.root.setOnLongClickListener {
            onLongClick(item)
            true
        }
        b.btnFavorite.setOnClickListener { onFavoriteClick(item) }
    }
}
