package com.example.workspace_booking_app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.fragment.app.DialogFragment
import com.example.workspace_booking_app.firebase.FirebaseWorkspaceRepo
import com.example.workspace_booking_app.supabase.SupabaseStorageHelper
import com.google.android.material.button.MaterialButton
import coil.load

class AddRoomDialogFragment : DialogFragment() {

    interface OnDialogCloseListener {
        fun onDialogClosed()
    }

    var onDialogCloseListener: OnDialogCloseListener? = null

    private val workspaceRepo = FirebaseWorkspaceRepo()
    private val storageHelper = SupabaseStorageHelper()
    private var newBannerUrl: String? = null

    private val pickMedia = registerForActivityResult(PickVisualMedia()) { uri ->
        if (uri != null) {
            val bannerImageView = view?.findViewById<ImageView>(R.id.imageViewBanner)

            // Upload to Supabase
            storageHelper.uploadImageFromUri(
                context = requireContext(),
                uri = uri,
                folder = "banners",
                onSuccess = { url ->
                    newBannerUrl = url
                    activity?.runOnUiThread {
                        bannerImageView?.load(url)
                        Toast.makeText(requireContext(), "Banner uploaded", Toast.LENGTH_SHORT).show()
                    }
                },
                onFailure = {
                    activity?.runOnUiThread {
                        Toast.makeText(requireContext(), "Upload failed", Toast.LENGTH_SHORT).show()
                    }
                }
            )
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

        // Load workspace from Firestore
        workspaceRepo.getOrCreateDefaultWorkspace(
            onSuccess = { workspace ->
                activity?.runOnUiThread {
                    nameTextView.setText(workspace["name"] as? String ?: "Default Workspace")
                    val bannerUrl = workspace["bannerUrl"] as? String
                    if (!bannerUrl.isNullOrEmpty()) {
                        bannerImageView.load(bannerUrl)
                    }
                }
            },
            onFailure = {
                activity?.runOnUiThread {
                    nameTextView.setText("Default Workspace")
                }
            }
        )

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

            workspaceRepo.updateWorkspace(
                name = workspaceName,
                bannerUrl = newBannerUrl,
                onSuccess = {
                    activity?.runOnUiThread {
                        Toast.makeText(requireContext(), "Workspace updated", Toast.LENGTH_SHORT).show()
                        onDialogCloseListener?.onDialogClosed()
                        dismiss()
                    }
                },
                onFailure = {
                    activity?.runOnUiThread {
                        Toast.makeText(requireContext(), "Update failed", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }
    }

    companion object {
        const val TAG = "AddRoomDialogFragment"
    }
}
