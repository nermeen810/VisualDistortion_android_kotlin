package com.rino.visualdestortion.ui.AddService

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.*
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.OnBackPressedCallback
import androidx.core.app.ActivityCompat
import androidx.core.view.isGone
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.*
import com.google.android.material.snackbar.Snackbar
import com.rino.visualdestortion.R
import com.rino.visualdestortion.databinding.FragmentAddServiceBinding
import com.rino.visualdestortion.model.pojo.addService.AddServiceResponse
import com.rino.visualdestortion.model.pojo.addService.FormData
import com.rino.visualdestortion.ui.home.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt


class AddServiceFragment : Fragment() {
    private lateinit var viewModel: AddServiceViewModel
    private lateinit var binding: FragmentAddServiceBinding
    private lateinit var addServiceResponse: AddServiceResponse
    private lateinit var sectorsList: ArrayList<String>
    private lateinit var municipalitesList: ArrayList<String>
    private lateinit var districtsList: ArrayList<String>
    private lateinit var streetList: ArrayList<String>
    private  var beforeImgBody: MultipartBody.Part? = null
    private  var afterImgBody: MultipartBody.Part? = null
    private lateinit  var formData: FormData
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private val CAMERA_REQUEST_CODE = 200
    private val REQUEST_CODE = 100
    private var serviceTypeId = ""
    private var serviceName = ""
    private var lat = ""
    private var lng = ""
    private var isSectorSelected = false
    private var isMunicipalitySelected = false
    private var isDistrictSelected = false
    private var isStreetSelected = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireActivity().application)
        viewModel = AddServiceViewModel(requireActivity().application)
        if (viewModel.isFirstTimeLaunch()) {
            viewModel.setFirstTimeLaunch(false)
            requestAllPermissions()
        }
        val onBackPressedCallback = object : OnBackPressedCallback(true) {
            @SuppressLint("ResourceType")
            override fun handleOnBackPressed() {
                findNavController().popBackStack()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }

    private fun requestAllPermissions() {
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            REQUEST_CODE
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAddServiceBinding.inflate(inflater, container, false)
        init()
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        (activity as MainActivity).bottomNavigation.isGone = true
    }

    override fun onStop() {
        super.onStop()
//        beforeImgBody = null
//        afterImgBody = null
        if (fusedLocationProviderClient != null) {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        }
    }

    private fun init() {
        getLatestLocation()
        setUpUI()
        initLists()
        observeData()

    }

    private fun setUpUI() {
        viewModel.getServicesData()
        if (getArguments() != null) {
            serviceName = getArguments()?.get("serviceName").toString()
            if (serviceName.equals("مخلفات الهدم")) {
                binding.textInputMSquare.isGone = true
            }
            else if (serviceName.equals("الكتابات المشوهة")) {
                binding.textInputMCube.isGone = true
                binding.textInputNumberR.isGone = true
            }
            else {
                binding.spicialItemsCard.isGone = true
                binding.spicialItemsTxt.isGone = true
            }
            binding.serviceTypeNameTxt.text = serviceName
            serviceTypeId = getArguments()?.get("serviceID").toString()
        }
        binding.notesEditTxt.addTextChangedListener {
            val notesLength =  binding.notesEditTxt.text.toString().length
            binding.notesLength.text = notesLength.toString()+" حرف "
        }
        binding.submitButton.setOnClickListener {
            submit()

        }
        binding.beforPic.setOnClickListener {
            beforePicOnClick()
        }
        binding.afterPic.setOnClickListener {
            afterPicOnClick()
        }
    }

    private fun submit() {
        formData = getFormDataFromUi(serviceName)
        if (validateData(formData) && lat != "" && lng != "") {
            val date = DateFormat.getDateInstance().format(Calendar.getInstance().time).toString()
            viewModel.getDailyPreparationByServiceID(serviceTypeId, date)
        }
    }

    private fun observeDailyPreparation() {
        viewModel.getDailyPreparation.observe(viewLifecycleOwner) {
            if (it != null) {
                formData.WorkersTypesList = it.workerTypesList
                formData.equipmentList = it.workerTypesList
                    viewModel.setFormData(formData)

//                else{
//                    getLatestLocation()
//                    if (validateData(formData) && lat != "" && lng != "") {
//                        viewModel.setFormData(formData)
//                    }
//               }
            }
        }
    }

    private fun getFormDataFromUi(serviceName: String): FormData {
   //     Toast.makeText(requireContext(),"Before : ${beforeImgBody.toString()}  ,Aftar : ${afterImgBody.toString()}",Toast.LENGTH_SHORT).show()
        var formData = FormData()
        if (serviceName == "مخلفات الهدم") {
            if(binding.editTextMCube.text.toString()!="")
            formData.mCube = binding.editTextMCube.text.toString().toInt()
            if(binding.editTextNumberR.text.toString()!="")
            formData.numberR = binding.editTextNumberR.text.toString().toInt()
        } else if (serviceName == "الكتابات المشوهة")
            if(binding.editTextMSquare.text.toString()!="")
            formData.mSquare = binding.editTextMSquare.text.toString().toInt()

        formData.serviceTypeId = serviceTypeId
        formData.sectorName = binding.sectorTextView.text.toString()
        formData.municipalityName = binding.municipalitesTextView.text.toString()
        formData.districtName = binding.districtsTextView.text.toString()
        formData.streetName = binding.streetTextView.text.toString()
        formData.lat = lat
        formData.lng = lng
     //   formData.WorkersTypesList = workerTypesAdapter.getWorkerTypesMap()
    //    formData.equipmentList = equipmentsAdapter.getEquipmentMap()
        formData.notes = binding.notesEditTxt.text.toString()
        if(beforeImgBody != null) {

            formData.beforeImg = beforeImgBody as MultipartBody.Part
        }
        Log.e("Image","Before : ${beforeImgBody.toString()}  ,Aftar : ${afterImgBody.toString()}")
        if(afterImgBody != null) {
            formData.afterImg = afterImgBody as MultipartBody.Part
        }
        formData.percentage = binding.precentageEditTxt.text.toString()
        return formData
    }

    private fun afterPicOnClick() {
        checkCameraPermission()
    }

    private fun beforePicOnClick() {
        checkExternalStoragePermission()
    }

    private fun initLists() {
        sectorsList = ArrayList()
        municipalitesList = ArrayList()
        districtsList = ArrayList()
        streetList = ArrayList()

    }

    private fun observeData() {
        observeGetServicesData()
        observeDailyPreparation()
        observeSetFormData()
        observeLoading()
        observeShowError()
    }

    private fun observeSetFormData() {
        viewModel.setServiceForm.observe(viewLifecycleOwner) {
            it?.let {
                navigateToQRCode(it.qrCode.toString())
            }
        }
    }

    private fun navigateToQRCode(qrCode: String) {
        val action = AddServiceFragmentDirections.actionAddServiceToQRCode(qrCode)
        findNavController().navigate(action)
    }



    private fun observeGetServicesData() {
        viewModel.getServicesData.observe(viewLifecycleOwner) {
            it.let {
                addServiceResponse = it
                prepareMenues()

            }
        }
    }

    private fun prepareMenues() {
        setSectorsMenuItems()
    }

    private fun setSectorsMenuItems() {
        sectorsList.clear()
        binding.sectorTextView.setText(R.string.sector)
        for (sector in addServiceResponse.sectors!!) {
            sectorsList.add(sector.name.toString())
        }
        val sectorsAdapter = ArrayAdapter(
            requireContext(), R.layout.dropdown_item,
            sectorsList
        )
        binding.sectorTextView.setAdapter(sectorsAdapter)
        binding.sectorTextView.onItemClickListener =
            AdapterView.OnItemClickListener { parent, view, position, id ->
                val selectedItem = parent.getItemAtPosition(position).toString()
                isSectorSelected = true
                setMunicipalitesMenuItems(position)
            }

    }

    private fun setMunicipalitesMenuItems(posSector: Int) {
        binding.municipalitesTextView.setText(R.string.municipalites)
        municipalitesList.clear()
        for (municipalite in addServiceResponse.sectors?.get(posSector)?.municipalites!!) {
            municipalitesList.add(municipalite.name.toString())
        }
        val municipalitesAdapter = ArrayAdapter(
            requireContext(),
            R.layout.dropdown_item,
            municipalitesList
        )
        binding.municipalitesTextView.setAdapter(municipalitesAdapter)
        binding.municipalitesTextView.onItemClickListener =
            AdapterView.OnItemClickListener { parent, view, position, id ->
                val selectedItem = parent.getItemAtPosition(position).toString()
                isMunicipalitySelected = true
                setDistrictsMenuItems(posSector, position)

            }
    }

    private fun setDistrictsMenuItems(posSector: Int, posMunicipalite: Int) {
        districtsList.clear()
        binding.districtsTextView.setText(R.string.districts)
        for (district in addServiceResponse.sectors?.get(posSector)?.municipalites?.get(
            posMunicipalite
        )?.districts!!) {
            districtsList.add(district.name.toString())
        }
        val districtsAdapter = ArrayAdapter(
            requireContext(), R.layout.dropdown_item,
            districtsList
        )
        binding.districtsTextView.setAdapter(districtsAdapter)
        binding.districtsTextView.onItemClickListener =
            AdapterView.OnItemClickListener { parent, view, position, id ->
                val selectedItem = parent.getItemAtPosition(position).toString()
                isDistrictSelected = true
                setStreetsMenuItems(posSector, posMunicipalite, position)
                // Display the clicked item using toast
                //   Toast.makeText(requireContext(),"Selected : $selectedItem",Toast.LENGTH_SHORT).show()
            }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun validateData(formData: FormData): Boolean {
        var flagSector = true
        var flagDistrict = true
        var flagMunicipality = true
        var flagStreet = true
        var flagBeforeImg = true
        var flagAfterImg = true
        var flagPrecentage = true
        var flagNumberR = true
        var flagMCube = true
        var flagMSquare = true
        var flagNotes = true
     //  Toast.makeText(requireContext(),"Selected : ${formData.sectorName == R.string.sector.toString()}",Toast.LENGTH_SHORT).show()
        if (!isSectorSelected) {
            binding.sectorTextInputLayout.error = "برجاء ادخال هذا العنصر"
            flagSector = false
        }
        else {
            binding.sectorTextInputLayout.error = null
            binding.sectorTextInputLayout.isErrorEnabled = false
            flagSector = true
        }

        if (!isDistrictSelected) {
            binding.districtsTextInputLayout.error = "برجاء ادخال هذا العنصر"
            flagDistrict = false
        }
        else {
            binding.districtsTextInputLayout.error = null
            binding.districtsTextInputLayout.isErrorEnabled = false
            flagDistrict = true
        }
        if (!isMunicipalitySelected) {
            binding.municipalitesTextInputLayout.error = "برجاء ادخال هذا العنصر"
            flagMunicipality = false
        }
        else {
            binding.municipalitesTextInputLayout.error = null
            binding.municipalitesTextInputLayout.isErrorEnabled = false
            flagMunicipality = true
        }
        if (!isStreetSelected) {
            binding.streetTextInputLayout.error = "برجاء ادخال هذا العنصر"
            flagStreet = false
        }
        else {
            binding.streetTextInputLayout.error = null
            binding.streetTextInputLayout.isErrorEnabled = false
            flagStreet = true
        }
        //binding.beforPic.drawable == resources.getDrawable(R.drawable.picture)
       // Toast.makeText(requireContext(),"Before : ${beforeImgBody == null}  ,Aftar : ${afterImgBody == null}",Toast.LENGTH_SHORT).show()
        if (beforeImgBody == null) {
            binding.textInputbeforeImg.error = "      مطلوب"
            flagBeforeImg = false
        }
        else {
            binding.textInputbeforeImg.error = null
            binding.textInputbeforeImg.isErrorEnabled = false
             flagBeforeImg = true
        }
        if (afterImgBody == null) {
            binding.textInputAfterImg.error = "      مطلوب"
            flagAfterImg = false
        }
        else {
            binding.textInputAfterImg.error = null
            binding.textInputAfterImg.isErrorEnabled = false
             flagAfterImg = true
        }
        if (formData.percentage == null || formData.percentage =="") {
            binding.textInputPercentage.error = "برجاء ادخال هذا العنصر"
            flagPrecentage = false
        }
        else {
            if (formData.percentage.toInt() > 100) {
                binding.textInputPercentage.error = "هذا العنصر يجب ان يكون بين 0:100 "
                flagPrecentage = false
            }
            else{
            binding.textInputPercentage.error = null
            binding.textInputPercentage.isErrorEnabled = false
                flagPrecentage = true
            }
        }

        if (formData.notes != null){
            if (formData.notes!!.length>500) {
               binding.textInputNotes.error = "هذا العنصر يجب ان يكون أقل من 500 حرف  "
                flagNotes = false
            }
            else{
                binding.textInputNotes.error = null
                binding.textInputNotes.isErrorEnabled = false
                 flagNotes = true
            }
        }
        if (serviceName.equals("مخلفات الهدم")) {
            if (formData.mCube == null) {
                binding.textInputMCube.error = "برجاء ادخال هذا العنصر"
                flagMCube = false
            }
            else{
                binding.textInputMCube.error = null
                binding.textInputMCube.isErrorEnabled = false
                flagMCube = true
            }
            if (formData.numberR == null) {
                binding.textInputNumberR.error = "برجاء ادخال هذا العنصر"
                flagNumberR = false
            }
            else{
                binding.textInputNumberR.error = null
                binding.textInputNumberR.isErrorEnabled = false
                flagNumberR = true
            }

        } else if (serviceName.equals("الكتابات المشوهة")) {
            if (formData.mSquare == null) {
                binding.textInputMSquare.error = "برجاء ادخال هذا العنصر"
                flagMSquare = false
            }
            else{
                binding.textInputMSquare.error = null
                binding.textInputMSquare.isErrorEnabled = false
                flagMSquare = true
            }

        }
      val flag =(flagSector && flagDistrict && flagMunicipality && flagStreet &&
              flagAfterImg && flagBeforeImg &&
              flagMCube && flagMSquare && flagNumberR &&
              flagPrecentage && flagNotes
                )
        return flag
    }

    private fun setStreetsMenuItems(posSector: Int, posMunicipalite: Int, posDistricts: Int) {
        streetList.clear()
        binding.streetTextView.clearListSelection()

        for (street in addServiceResponse.sectors?.get(posSector)?.municipalites?.get(
            posMunicipalite
        )?.districts?.get(posDistricts)?.streets!!) {
            streetList.add(street.name.toString())
        }
        val streetsAdapter = ArrayAdapter(
            requireContext(), R.layout.dropdown_item,
            streetList
        )
        binding.streetTextView.setAdapter(streetsAdapter)
        binding.streetTextView.onItemClickListener =
            AdapterView.OnItemClickListener { parent, view, position, id ->
                val selectedItem = parent.getItemAtPosition(position).toString()
                isStreetSelected = true
                //  setStreetsMenuItems(posSector,posMunicipalite,position)
                // Display the clicked item using toast
                // Toast.makeText(requireContext(),"Selected : $selectedItem",Toast.LENGTH_SHORT).show()
            }
    }




    private fun observeLoading() {
        viewModel.loading.observe(viewLifecycleOwner) {
            it?.let {
                binding.progress.visibility = it
            }
        }
    }

    private fun observeShowError() {
        viewModel.setError.observe(viewLifecycleOwner) {
            it?.let {
                Snackbar.make(requireView(), it, Snackbar.LENGTH_INDEFINITE)
                    .setAnimationMode(Snackbar.ANIMATION_MODE_SLIDE).setBackgroundTint(
                        getResources().getColor(
                            R.color.teal
                        )
                    )
                    .setActionTextColor(getResources().getColor(R.color.white)).setAction("Ok")
                    {
                    }.show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == CAMERA_REQUEST_CODE && data != null) {
            binding.afterPic.setImageBitmap(data.extras?.get("data") as Bitmap)
            var bitmap = data.extras?.get("data") as Bitmap
            val afterBitmap =bitmap.copy(Bitmap. Config.ARGB_8888,true)
            CoroutineScope(Dispatchers.Default).launch {
                binding.afterPic.setImageBitmap(
                    drawTextToBitmap(
                        afterBitmap,
                        3,
                        Calendar.getInstance().time.toString()
                    )
                )
                CoroutineScope(Dispatchers.Main).launch{
                    try {
                        val file = File(getRealPathFromURI(getImageUri(requireContext(), afterBitmap)!!))
                        println("afterfilePath" + file.path)
                        if (file != null) {
                            val requestFile: RequestBody =
                                RequestBody.create("multipart/form-data".toMediaTypeOrNull(), file!!)
                            afterImgBody = MultipartBody.Part.createFormData(
                                "afterImg",
                                file?.name.trim(),
                                requestFile
                            )
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
        }
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE) {
            binding.beforPic.setImageURI(data?.data) // handle chosen image
            //    var bitmap = data?.data as Bitmap
            val bitmap = MediaStore.Images.Media.getBitmap(
                requireActivity().getContentResolver(),
                data?.data
            )
            val beforeBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
            CoroutineScope(Dispatchers.Default).launch {
                binding.beforPic.setImageBitmap(
                    drawTextToBitmap(
                        beforeBitmap,
                        3,
                        Calendar.getInstance().time.toString()
                    )
                )
                CoroutineScope(Dispatchers.Main).launch {
                    try {

//                val file = File(
//                    getRealPathFromURI(data?.data!!)
//                )
                        val file =
                            File(getRealPathFromURI(getImageUri(requireContext(), beforeBitmap)!!))
                        println("beforefilePath" + file.path)
                        if (file != null) {
                            val requestFile: RequestBody =
                                RequestBody.create(
                                    "multipart/form-data".toMediaTypeOrNull(),
                                    file!!
                                )
                            beforeImgBody = MultipartBody.Part.createFormData(
                                "beforeImg",
                                file?.name.trim(),
                                requestFile
                            )
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }
    private fun drawTextToBitmap(bitmap: Bitmap, textSize: Int = 2, text: String): Bitmap {

        val canvas = Canvas(bitmap)
        // new antialised Paint - empty constructor does also work
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.color = Color.RED
        val width: Int = bitmap.width
        val height: Int = bitmap.height
        val radius = if (width > height) height / 2 else width / 2
        val center_x = width / 2
        val center_y = height / 2
        // text size in pixels
        val scale = resources.displayMetrics.density
        paint.textSize = (textSize * scale).roundToInt().toFloat()

        //custom fonts or a default font
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)

        // draw text to the Canvas center
        val bounds = Rect()
        //draw the text
        paint.getTextBounds(text, 0, text.length, bounds)
        val rectF = RectF(
            200f,
            180f,
            canvas.width - 80F,
            canvas.height - 120F
        )
        canvas.drawRect(rectF,Paint().apply { color = Color.RED })

        //x and y defines the position of the text, starting in the top left corner
        paint.color = Color.WHITE
        canvas.drawText(text, 10f, 20f, paint)
        return bitmap
    }

    fun getImageUri(inContext: Context, inImage: Bitmap): Uri? {
        val bytes = ByteArrayOutputStream()
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val sdf = SimpleDateFormat("dd/M/yyyy hh:mm:ss")
        val currentDate = sdf.format(Date())
        val path = MediaStore.Images.Media.insertImage(
            inContext.contentResolver,
            inImage,
            "AFTER_IMG_" + currentDate.toString().replace(" ",""),
            null
        )
//        val path = MediaStore.Images.Media.insertImage(
//            inContext.contentResolver,
//            inImage,
//            "After_IMG_",
//            null
//        )
        return Uri.parse(path)
    }

    fun getRealPathFromURI(uri: Uri): String? {
        val cursor: Cursor? =
            requireActivity().getContentResolver().query(uri, null, null, null, null)
        cursor?.moveToFirst()
        val idx: Int? = cursor?.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
        return idx?.let { cursor.getString(it) }
    }

    @SuppressLint("MissingPermission")
    fun getLatestLocation() {
        if (isPermissionGranted()) {
            if (checkLocation()) {
                val locationRequest = LocationRequest()
                with(locationRequest) {
                    priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                    interval = 1000
                }
                fusedLocationProviderClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    Looper.getMainLooper()
                )
            } else {
                enableLocationPermission()
            }
        } else {
          //  requestPermission()
            navigateToAppSetting()
        }
    }

    private fun isPermissionGranted(): Boolean {
        return ActivityCompat.checkSelfPermission(
            requireActivity().application,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(
                    requireActivity().application,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    private fun checkLocation(): Boolean {
        val locationManager =
            requireActivity().application.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            return true
        } else {
            return false;
        }
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val location = locationResult.lastLocation
            lng = location.longitude.toString()
            lat = location.latitude.toString()
            //   Toast.makeText(context, "lat:" + lat + ", lng:" + lng, Toast.LENGTH_SHORT).show()

        }
    }

    private fun enableLocationPermission() {
        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        startActivity(intent)
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            ),
            1
        )
    }

    //camera permission
    @SuppressLint("MissingPermission")
    fun checkCameraPermission() {
        if (!isCameraPermissionGranted()) {
            navigateToAppSetting()
        } else {
            enableCamera()
        }
    }

    private fun isCameraPermissionGranted(): Boolean {
        return ActivityCompat.checkSelfPermission(
            requireActivity().application,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun navigateToAppSetting() {
        startActivity(Intent().apply {
            action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            data = Uri.fromParts("package", requireContext().packageName, null)
        })
    }

    private fun enableCamera() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE)
    }

    //external storage
    @SuppressLint("MissingPermission")
    fun checkExternalStoragePermission() {
        if (!isExternalStoragePermissionGranted()) {
            navigateToAppSetting()
        } else {
            enablePhotoes()
        }
    }

    private fun enablePhotoes() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_CODE)
    }

    private fun isExternalStoragePermissionGranted(): Boolean {
        return ActivityCompat.checkSelfPermission(
            requireActivity().application,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

//    val requestExternalStoragePermissionLauncher =
//        registerForActivityResult(
//            ActivityResultContracts.RequestPermission()
//        ) { isGranted: Boolean ->
//            if (!isGranted) {
//
//            } else {
//
//            }
//        }


}
