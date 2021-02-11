package com.example.jciclient

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.navArgs

class YesNoDialogFragment : DialogFragment() {

    private val args by navArgs<YesNoDialogFragmentArgs>()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(requireContext()).apply {
            setMessage(args.message)
            setPositiveButton(args.positive) { dialog, _ ->
                setFragmentResult(args.request, bundleOf())
                dialog.dismiss()
            }
            setNegativeButton(args.negative) { dialog, _ ->
                dialog.dismiss()
            }
        }.create()
    }
}