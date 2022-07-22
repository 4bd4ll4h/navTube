package com.abd4ll4h.navtube

import android.app.Dialog
import android.content.Context
import android.content.ContextWrapper
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.ContextThemeWrapper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.core.content.res.ResourcesCompat
import androidx.core.widget.addTextChangedListener
import androidx.navigation.fragment.findNavController
import androidx.sqlite.db.SupportSQLiteCompat.Api16Impl.cancel
import com.abd4ll4h.navtube.databinding.FragmentCommuntiyBinding
import com.abd4ll4h.navtube.databinding.FragmentFavBinding
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.input.input
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.dialog.MaterialDialogs
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class FavFragment : Fragment() {

    private var _binding: FragmentFavBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentFavBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.addLabel.setOnClickListener {
            val con =ContextWrapper(this.requireContext())
            con.setTheme(R.style.PopupDialog)

            val textInputLayout=TextInputLayout(con)
            textInputLayout.boxBackgroundMode = TextInputLayout.BOX_BACKGROUND_OUTLINE
            textInputLayout.isCounterEnabled=true
            textInputLayout.setPadding(getDimensionFromAttribute(requireContext(),android.R.attr.dialogPreferredPadding),
                getDimensionFromAttribute(requireContext(),android.R.attr.dialogPreferredPadding),8,0)
            textInputLayout.setBoxCornerRadii(10f,10f,10f,10f)
            textInputLayout.counterMaxLength=15
           val editText= TextInputEditText(textInputLayout.context)
            editText.setHint(R.string.label_name)
            textInputLayout.addView(editText)
            val dialog=MaterialAlertDialogBuilder(requireContext(),R.style.PopupDialog)
                .setView(textInputLayout)

                .setPositiveButton(R.string.save) { dialog, which ->

                    Toast.makeText(this.context,editText.text,Toast.LENGTH_LONG).show()
                }
                .setNegativeButton(R.string.cancel,null)
                .create()

            dialog.show()
            dialog.getButton(Dialog.BUTTON_POSITIVE).isClickable=false
            dialog.getButton(Dialog.BUTTON_POSITIVE).alpha=0.5f
            this.requireContext().setTheme(R.style.Base_Theme_NavTube)

            editText.addTextChangedListener {
                text ->
                if (text!!.isNotEmpty() && text.length<=15)
                {
                    dialog.getButton(Dialog.BUTTON_POSITIVE).isClickable=true
                    dialog.getButton(Dialog.BUTTON_POSITIVE).alpha=1f
                }else{
                    dialog.getButton(Dialog.BUTTON_POSITIVE).isClickable=false
                    dialog.getButton(Dialog.BUTTON_POSITIVE).alpha=0.5f

                }


            }


        }

    }

    fun getDimensionFromAttribute(context: Context, attr: Int): Int {
        val typedValue = TypedValue()
        return if (context.theme.resolveAttribute(attr, typedValue, true))
            TypedValue.complexToDimensionPixelSize(typedValue.data, context.resources.displayMetrics)
        else 0
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}