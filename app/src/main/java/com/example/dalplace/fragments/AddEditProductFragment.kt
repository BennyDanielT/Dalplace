package com.example.dalplace.fragments

import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.View.OnFocusChangeListener
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.databinding.adapters.TextViewBindingAdapter.OnTextChanged
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.example.dalplace.R
import com.example.dalplace.databinding.FragmentAddEditProductBinding
import com.example.dalplace.model.Category
import com.example.dalplace.model.Product
import com.example.dalplace.utilities.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import org.jetbrains.annotations.NotNull
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


class AddEditProductFragment : Fragment(), OnClickListener, OnFocusChangeListener {

    private val REQUEST_IMAGE_CAPTURE = 1001
    private val REQUEST_IMAGE_PICK = 1002
    private val PERMISSION_REQUEST_CAMERA = 10001

    private var binding: FragmentAddEditProductBinding? = null
    var progressDialog: ProgressDialog? = null
    private var imageUri: Uri? = null
    private var isNew = true
    private var selectedProduct: Product? = null
    private var categories: MutableList<Category>? = mutableListOf()

    /*********************************************************************************************
     * FRAGMENT LIFECYCLES
     *********************************************************************************************/

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentAddEditProductBinding.inflate(layoutInflater)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
    }

    override fun onAttach(@NotNull context: Context) {
        super.onAttach(context)

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                cancelPressed()
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
    }

    /*********************************************************************************************
     * VIEW/WIDGET INITIALIZATION
     *********************************************************************************************/

    private fun initViews() {
        getFragmentArguments()
        getProductCategories()
        initProgressDialog()
        setOnClick()
        setOnFocusChange()
        setOnTextChanged()

        // Edit Mode
        initEditMode()
    }

    private fun initProgressDialog() {
        progressDialog = ProgressDialog(context)
        progressDialog?.setCancelable(false)
        progressDialog?.setMessage("Loading...")
    }

    private fun showProgressDialog(message: String = "Loading...") {
        progressDialog?.let {
            it.setMessage(message)
            if (!it.isShowing) it.show()
        }
    }

    private fun hideProgressDialog() {
        progressDialog?.let {
            if (it.isShowing) it.dismiss()
        }
    }

    private fun getFragmentArguments() {
        arguments?.let {
            isNew = AddEditProductFragmentArgs.fromBundle(it).isNew
            selectedProduct = AddEditProductFragmentArgs.fromBundle(it).selectedProduct
        }
    }

    private fun initSpCategory() {

        val arrayAdapter = ArrayAdapter(requireActivity(), R.layout.sp_category_item, categories!!);
        binding?.spAutoCompleteCategory?.setAdapter(arrayAdapter)

        if (!isNew) {
            binding?.spAutoCompleteCategory?.setText(selectedProduct?.category, false)
        }

        binding?.spAutoCompleteCategory?.setOnItemClickListener { adapterView, view, position, id ->
            val category = binding?.spAutoCompleteCategory?.text?.toString()
            if (Validation.validString(category)) {
                binding?.spCategory?.helperText = null
            }
        }
    }

    private fun initEditMode() {

        if (isNew) return
        // Update Button Text
        binding?.btnSave?.text = if (isNew) "Post" else "Update"

        selectedProduct?.let {
            // Update ImageView
            Picasso.get()
                .load(it.imageUrl)
                .placeholder(R.drawable.ic_baseline_image_24)
                .error(R.drawable.ic_baseline_error_outline_24)
                .into(binding?.ivProductImage)

            // Update Texts
            binding?.etTitle?.setText(it.title)
            binding?.etDescription?.setText(it.description)
            binding?.etPrice?.setText(it.price.toString())
            binding?.cbEnableChat?.isChecked = it.isChatEnabled
            binding?.cbUrgentRequest?.isChecked = it.isUrgentRequest

            // Update Helper texts
            binding?.tilTitle?.helperText = null
            binding?.tilDescription?.helperText = null
            binding?.tilPrice?.helperText = null
            binding?.spCategory?.helperText = null

            binding?.cbUrgentRequest?.isEnabled = false
        }
    }

    private fun setOnFocusChange() {
        binding?.tilTitle?.editText?.onFocusChangeListener = this
        binding?.tilDescription?.editText?.onFocusChangeListener = this
        binding?.tilPrice?.editText?.onFocusChangeListener = this
    }

    private fun setOnTextChanged() {
        binding?.tilTitle?.editText?.doAfterTextChanged {
            Validation.validateTilStringInput(binding?.tilTitle)
        }

        binding?.tilDescription?.editText?.doAfterTextChanged {
            Validation.validateTilStringInput(binding?.tilDescription)
        }

        binding?.tilPrice?.editText?.doAfterTextChanged {
            Validation.validateTilCurrencyInput(binding?.tilPrice)
        }
    }

    private fun setOnClick() {
        binding?.ivProductImage?.setOnClickListener(this)
        binding?.btnCancel?.setOnClickListener(this)
        binding?.btnSave?.setOnClickListener(this)
        binding?.ibCamera?.setOnClickListener(this)
        binding?.ibGallery?.setOnClickListener(this)
    }

    /*********************************************************************************************
     * ON CLICK LISTENER IMPLEMENTATION
     *********************************************************************************************/

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.ibGallery -> {
                openGallery()
            }
            R.id.ibCamera -> {
                if (Permissions.hasCameraPermission(requireContext())) {
                    Log.d("OpenCamera", "Before Calling Camera")
                    openCamera()
                } else {
                    Log.d("OpenCamera", "Before Getting Permissions")
                    getCameraPermissions()
                }
            }
            R.id.btnCancel -> {
                cancelPressed()
            }
            R.id.btnSave -> {
                submitForm()
            }
        }
    }

    /*********************************************************************************************
     * ON FOCUS CHANGE LISTENER IMPLEMENTATION
     *********************************************************************************************/

    override fun onFocusChange(view: View?, isFocused: Boolean) {
        when (view?.id) {
            R.id.etTitle -> {
                if (isFocused) return
                Validation.validateTilStringInput(binding?.tilTitle)
            }
            R.id.etDescription -> {
                if (isFocused) return
                Validation.validateTilStringInput(binding?.tilDescription)
            }
            R.id.etPrice -> {
                if (isFocused) return
                Validation.validateTilCurrencyInput(binding?.tilPrice)
            }
        }
    }

    /*********************************************************************************************
     * PRODUCT IMAGE TASKS
     *********************************************************************************************/

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            PERMISSION_REQUEST_CAMERA -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(context, "Camera Permission Denied", Toast.LENGTH_SHORT).show()
                } else {
                    openCamera()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_IMAGE_CAPTURE -> {
                if (resultCode == RESULT_OK) {
                    binding?.ivProductImage?.setImageURI(imageUri)
                }
            }
            REQUEST_IMAGE_PICK -> {
                imageUri = data?.data
                binding?.ivProductImage?.setImageURI(imageUri)
            }
        }
    }

    private fun getCameraPermissions() {
        Permissions.requestCameraPermission(requireActivity(), PERMISSION_REQUEST_CAMERA)
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        imageUri = FileProvider.getUriForFile(
            requireContext(),
            requireContext().applicationContext.packageName + ".provider",
            createImageFile()
        )
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_IMAGE_PICK)
    }

    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val userId = Firebase.userId
        val fileName = "Product_${userId}_${timeStamp}.jpg"
        val externalFilesDir = requireContext()
            .getExternalFilesDir(Environment.DIRECTORY_PICTURES)

        return File(externalFilesDir, fileName)
    }

    /*********************************************************************************************
     * FORM VALIDATIONS AND UPDATES
     *********************************************************************************************/

    private fun isFormStarted(): Boolean {
        return (isNew &&
                (imageUri != null
                        || binding?.spAutoCompleteCategory?.text.toString().isNotBlank()
                        || binding?.etTitle?.text.toString().isNotBlank()
                        || binding?.etDescription?.text.toString().isNotBlank()
                        || binding?.etPrice?.text.toString().isNotBlank()
                        || !binding?.cbEnableChat?.isChecked!!
                        || binding?.cbUrgentRequest?.isChecked!!))
    }

    private fun isFormValid(): Boolean {
        val validTitle = binding?.tilTitle?.helperText.isNullOrBlank()
        val validDescription = binding?.tilDescription?.helperText.isNullOrBlank()
        val validPrice = binding?.tilPrice?.helperText.isNullOrBlank()
        val validCategory = binding?.spCategory?.helperText.isNullOrBlank()


        if (!validTitle) {
            val message = "Title: ${binding?.tilTitle?.helperText}!"
            showValidationAlertBox(message)
            return false
        } else if (!validDescription) {
            val message = "Description: ${binding?.tilDescription?.helperText}!"
            showValidationAlertBox(message)
            return false
        } else if (!validPrice) {
            val message = "Price: ${binding?.tilPrice?.helperText}!"
            showValidationAlertBox(message)
            return false
        } else if (!validCategory) {
            val message = "Category: ${binding?.spCategory?.helperText}!"
            showValidationAlertBox(message)
            return false
        } else if (isNew && imageUri == null) {
            val message = "Product Image: Required!"
            showValidationAlertBox(message)
            return false
        }
        return true
    }

    private fun isFormUpdated(): Boolean {

        if (isNew || selectedProduct == null) return false

        val category = binding?.spAutoCompleteCategory?.text.toString()
        val title = binding?.etTitle?.text.toString()
        val description = binding?.etDescription?.text.toString()
        val price = binding?.etPrice?.text.toString().toDoubleOrNull()
        val isChatEnabled = binding?.cbEnableChat?.isChecked ?: false
        val isUrgentRequest = binding?.cbUrgentRequest?.isChecked ?: false

        selectedProduct?.let {
            if (it.category != category
                || it.title != title
                || it.description != description
                || it.price != price
                || it.isChatEnabled != isChatEnabled
                || it.isUrgentRequest != isUrgentRequest
            ) {
                return true
            }
        }

        return false
    }

    private fun isImageUpdated(): Boolean {
        if (isNew || selectedProduct == null) return false

        return imageUri != null
    }

    private fun showValidationAlertBox(message: String) {
        AlertDialog.Builder(context)
            .setTitle("Invalid Form")
            .setMessage(message)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                //Close the Dialog Box
            }.show()
    }

    private fun submitForm() {
        if (!isFormValid()) return

        // Read Data
        val ownerId = Firebase.userId
        val category = binding?.spAutoCompleteCategory?.text.toString()
        val title = binding?.etTitle?.text.toString()
        val description = binding?.etDescription?.text.toString()
        val price = binding?.etPrice?.text.toString().toDoubleOrNull()
        val isChatEnabled = binding?.cbEnableChat?.isChecked ?: false
        val isUrgentRequest = binding?.cbUrgentRequest?.isChecked ?: false

        val product = Product(
            ownerId = ownerId,
            category = category,
            title = title,
            description = description,
            price = price,
            isChatEnabled = isChatEnabled,
            isUrgentRequest = isUrgentRequest,
            isSold = false
        )

        if (isNew) {
            addProduct(product)
        } else {
            product.id = selectedProduct?.id
            product.imageUrl = selectedProduct?.imageUrl
            product.owner = selectedProduct?.owner
            if (isFormUpdated()) {
                updateProduct(product)
            } else {
                uploadImage(ownerId!!, product.id!!)
            }
        }
    }

    /*********************************************************************************************
     * FIREBASE CALLS
     *********************************************************************************************/

    private fun getProductCategories() {
        val firestore = FirebaseFirestore.getInstance()

        firestore.collection(Firebase.COLLECTION_PRODUCT_CATEGORIES)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    categories = task.result.toObjects(Category::class.java)
                } else {
                    task.exception?.printStackTrace()
                    categories?.let {
                        it.add(Category("Furniture"))
                        it.add(Category("Electronics"))
                        it.add(Category("Books"))
                        it.add(Category("Others"))
                    }
                }
                categories?.sortBy { category -> category.name }
                initSpCategory()
            }
    }

    private fun addProduct(product: Product) {
        val firestore = FirebaseFirestore.getInstance()

        val date = Calendar.getInstance().time
        product.lastUpdated = date

        showProgressDialog("Adding Product...")
        firestore
            .collection(Firebase.COLLECTION_PRODUCTS)
            .add(product)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    uploadImage(product.ownerId!!, task.result.id)

                    val title = "New Item Request - Urgent"
                    val message =
                        "${LoginInfo.user?.name} requested for - ${product.title}"
                    PushNotification.sendNotificationToAllUsers(title, message)
                } else {
                    hideProgressDialog()
                    Toast.makeText(context, task.exception?.message, Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun updateProduct(product: Product) {
        val firestore = FirebaseFirestore.getInstance()

        val date = Calendar.getInstance().time
        product.lastUpdated = date

        showProgressDialog("Updating Product...")
        firestore
            .collection(Firebase.COLLECTION_PRODUCTS)
            .document(product.id!!)
            .set(product)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(context, "Product Updated", Toast.LENGTH_SHORT).show()
                    uploadImage(product.ownerId!!, product.id!!)
                } else {
                    Toast.makeText(context, task.exception?.message, Toast.LENGTH_SHORT).show()
                }
                hideProgressDialog()
            }
    }

    private fun uploadImage(userId: String, productId: String) {
        if (!isNew && !isImageUpdated()) {
            goBack()
            return
        }

        val storage = FirebaseStorage.getInstance().reference
        val imageRef = storage.child(Firebase.BUCKET_PRODUCT).child(userId).child(productId)

        showProgressDialog("Uploading Image...")
        imageRef
            .putFile(imageUri!!)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    imageRef.downloadUrl.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            updateProductWithImageUrl(productId, task.result.toString())
                        } else {
                            Toast.makeText(context, task.exception?.message, Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                } else {
                    hideProgressDialog()
                    Toast.makeText(context, task.exception?.message, Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun updateProductWithImageUrl(productId: String, imageUrl: String) {
        val firestore = FirebaseFirestore.getInstance()

        val date = Calendar.getInstance().time

        showProgressDialog("Updating Product...")
        firestore
            .collection(Firebase.COLLECTION_PRODUCTS)
            .document(productId)
            .update("imageUrl", imageUrl, "lastUpdated", date)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    hideProgressDialog()
                    val message = if (isNew) "Product Added" else "Image Updated"
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                } else {
                    hideProgressDialog()
                    Toast.makeText(context, task.exception?.message, Toast.LENGTH_SHORT).show()
                }
                goBack()
            }
    }

    /*********************************************************************************************
     * NAVIGATION
     *********************************************************************************************/

    private fun cancelPressed() {
        if ((isNew && isFormStarted())
            || isFormUpdated()
            || isImageUpdated()
        ) {
            showCancelAlertBox()
        } else {
            goBack()
        }
    }

    private fun goBack() {
        Log.d("OnBackPressed", "goBack()")
        Navigation.findNavController(binding!!.root).popBackStack()
    }

    private fun showCancelAlertBox() {
        AlertDialog.Builder(context)
            .setTitle("Are you sure?")
            .setMessage("Discard Changes?")
            .setPositiveButton(android.R.string.ok) { _, _ ->
                goBack()
            }
            .setNegativeButton(android.R.string.cancel) { _, _ ->
                // Close Dialog Box
            }
            .show()
    }
}