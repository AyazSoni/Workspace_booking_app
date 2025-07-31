package com.example.workspace_booking_app

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.fragment.app.DialogFragment
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import com.example.workspace_booking_app.sharedstore.Workspace
import com.example.workspace_booking_app.utils.ImageUtils
import com.google.android.material.button.MaterialButton
import java.io.File

class AddRoomDialogFragment : DialogFragment() {

    interface OnDialogCloseListener {
        fun onDialogClosed()
    }

    var onDialogCloseListener: OnDialogCloseListener? = null

    private val pickMedia = registerForActivityResult(PickVisualMedia()) { uri ->
        if (uri != null) {
            val path = ImageUtils.handleImageSelection(requireContext(), uri, "banner")
            Toast.makeText(requireContext(), "Image Saved", Toast.LENGTH_SHORT).show()
            val bannerImageView = view?.findViewById<ImageView>(R.id.imageViewBanner)
            ImageUtils.setImageFromPath(bannerImageView, path)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.admin_setting_dialog, container, false)
        val nameTextView = view.findViewById<EditText>(R.id.workspace_name_input)
        val bannerImageView = view.findViewById<ImageView>(R.id.imageViewBanner)
        val workspace_name = Workspace.getWorkspaceName()
        val bannerPath = Workspace.getWorkspaceBannerPath()
        nameTextView.setText(workspace_name)
        bannerPath?.let {
            ImageUtils.setImageFromPath(bannerImageView, it)
        }

        return view
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.9).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<ImageButton>(R.id.btnClose).setOnClickListener {
            dismiss()
        }

        view.findViewById<MaterialButton>(R.id.btnChangeBanner).setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly))
        }

        view.findViewById<MaterialButton>(R.id.btnsave).setOnClickListener {
            val workspaceName = view.findViewById<EditText>(R.id.workspace_name_input).text.toString()
            Workspace.postWorkspaceData(workspaceName)
            onDialogCloseListener?.onDialogClosed()
            dismiss()
        }
    }

    companion object {
        const val TAG = "AddRoomDialogFragment"
    }
}
