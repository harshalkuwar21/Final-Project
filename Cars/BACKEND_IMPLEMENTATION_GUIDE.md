============================================================================
CAR ADD ENHANCEMENT - BACKEND IMPLEMENTATION GUIDE
============================================================================

This document outlines the backend changes required to complete the car add 
enhancement with showroom assignment and improved car information fields.

============================================================================
FILES ALREADY CREATED/UPDATED:
============================================================================

1. ✅ car-add.html (UPDATED) - Enhanced form with new fields
2. ✅ car-add-v2.html (NEW) - Alternative version with full styling
3. ✅ car-add-enhanced.css (NEW) - Complete responsive CSS
4. ✅ car-add-enhanced.js (NEW) - Enhanced JS with all fields
5. ✅ showroom-cars.html (NEW) - Showroom-wise car view template
6. ✅ CarController.java (PARTIALLY UPDATED) - Needs completion

============================================================================
BACKEND CHANGES REQUIRED:
============================================================================

1. UPDATE: CarController.java
   ─────────────────────────────────────
   
   ADD these imports:
   ```java
   import com.Dk3.Cars.repository.ShowroomRepository;
   ```

   ADD these fields in the class:
   ```java
   @Autowired
   private ShowroomRepository showroomRepository;
   ```

   UPDATE the /add GET mapping (CURRENT issue - needs fixing):
   ```java
   @GetMapping("/add")
   public String showAddCarForm(Model model) {
       model.addAttribute("showrooms", showroomRepository.findAll());
       return "car-add-v2"; // or "car-add" based on your choice
   }
   ```

   ADD this new GET mapping for showroom view:
   ```java
   @GetMapping("/showroom/{id}")
   public String showCarsInShowroom(@PathVariable Long id, Model model) {
       model.addAttribute("cars", carRepository.findByShowroomIdOrderByIdDesc(id));
       model.addAttribute("showroomId", id);
       return "showroom-cars";
   }
   ```

   NOTE: You need to add ShowroomRepository as @Autowired or use carService.getCarRepository()


2. UPDATE: DashboardRestController.java
   ──────────────────────────────────────
   
   ADD this import at the top with other imports:
   ```java
   import java.time.LocalDate;
   ```

   REPLACE the entire @PostMapping("/cars/add") method with:
   ```java
   @PostMapping(value = "/cars/add", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
   public Map<String, Object> addCar(
           @RequestParam(value = "brand", required = false) String brand,
           @RequestParam(value = "model", required = false) String model,
           @RequestParam(value = "variant", required = false) String variant,
           @RequestParam(value = "color", required = false) String color,
           @RequestParam(value = "fuelType", required = false) String fuelType,
           @RequestParam(value = "transmission", required = false) String transmission,
           @RequestParam(value = "price", required = false) Double price,
           @RequestParam(value = "vin", required = false) String vin,
           @RequestParam(value = "engineNo", required = false) String engineNo,
           @RequestParam(value = "purchaseDate", required = false) String purchaseDate,
           @RequestParam(value = "supplierInfo", required = false) String supplierInfo,
           @RequestParam(value = "showroom", required = false) Long showroomId,
           @RequestParam(value = "status", required = false) String status,
           @RequestParam(value = "stockQuantity", required = false) Integer stockQuantity,
           @RequestParam(value = "images", required = false) MultipartFile[] images
   ) {
       Map<String, Object> response = new HashMap<>();

       try {
           Car car = new Car();
           if (brand != null) car.setBrand(brand.trim());
           if (model != null) car.setModel(model.trim());
           if (variant != null) car.setVariant(variant.trim());
           if (color != null) car.setColor(color.trim());
           if (fuelType != null) car.setFuelType(fuelType);
           if (transmission != null) car.setTransmission(transmission);
           if (price != null && price > 0) car.setPrice(price);
           car.setStatus((status != null && !status.isEmpty()) ? status : "Available");
           if (stockQuantity != null) car.setStockQuantity(stockQuantity);
           
           // NEW: Enhanced car info fields
           if (vin != null && !vin.isEmpty()) car.setVin(vin.trim());
           if (engineNo != null && !engineNo.isEmpty()) car.setEngineNo(engineNo.trim());
           if (supplierInfo != null && !supplierInfo.isEmpty()) car.setSupplierInfo(supplierInfo.trim());
           
           // NEW: Parse purchase date
           if (purchaseDate != null && !purchaseDate.isEmpty()) {
               try {
                   car.setPurchaseDate(LocalDate.parse(purchaseDate));
               } catch (Exception e) {
                   logger.warn("Failed to parse purchase date: {}", e.getMessage());
               }
           }
           
           // NEW: Assign to showroom
           if (showroomId != null && showroomId > 0) {
               Showroom showroom = showroomRepository.findById(showroomId).orElse(null);
               if (showroom != null) {
                   car.setShowroom(showroom);
               }
           }

           // Handle image uploads
           List<String> imageUrls = new ArrayList<>();
           if (images != null) {
               for (MultipartFile file : images) {
                   if (file != null && !file.isEmpty()) {
                       try {
                           String url = carService.uploadCarImage(file);
                           if (url != null) imageUrls.add(url);
                       } catch (IOException ioe) {
                           logger.error("Failed to upload image: {}", ioe.getMessage());
                       }
                   }
               }
           }
           if (!imageUrls.isEmpty()) car.setImageUrls(imageUrls);

           Car saved = carService.saveCar(car);

           response.put("success", true);
           response.put("message", "Vehicle added successfully!");
           response.put("car", saved);
           return response;

       } catch (Exception e) {
           response.put("success", false);
           response.put("message", "Error adding car: " + e.getMessage());
           return response;
       }
   }
   ```


3. UPDATE: CarService.java
   ──────────────────────────
   
   ADD this method to expose CarRepository (if needed by CarController):
   ```java
   public CarRepository getCarRepository() {
       return carRepository;
   }
   ```


============================================================================
CONFIGURATION CHOICES:
============================================================================

OPTION 1 - Use car-add.html (existing, already updated)
   - Uses /css/car-add.css with basic styling
   - Integrate your current CSS enhancements

OPTION 2 - Use car-add-v2.html (NEW, recommended)
   - References /css/car-add-enhanced.css (complete modern styling)
   - Better UI/UX with form sections and icons
   - Update CarController to return "car-add-v2"

OPTION 3 - Merge both
   - Copy CSS from car-add-enhanced.css into car-add.css
   - Use original car-add.html
   - Keep existing JavaScript references


============================================================================
WHICH FILES TO USE:
============================================================================

RECOMMENDED SETUP:
   1. CarController: Add @Autowired ShowroomRepository
   2. CarController: Update GET /cars/add to pass showrooms
   3. CarController: Add GET /cars/showroom/{id} method
   4. DashboardRestController: Update @PostMapping /cars/add with all new params
   5. Use template: car-add-v2.html (rename to car-add.html or update reference)
   6. Use CSS: car-add-enhanced.css (rename to car-add.css or update reference)
   7. Use JS: car-add-enhanced.js (or update existing car-add.js)


============================================================================
JAVASCRIPT FILE CHOICE:
============================================================================

CURRENT: car-add.js
   - Working but basic functionality
   - Needed: Update FormData field selector (already fixed)

NEW: car-add-enhanced.js
   - Handles all fields (vin, engineNo, purchaseDate, supplierInfo, showroom)
   - Better error handling
   - Redirect to showroom view after success
   - Recommended to use this

ACTION: Update car-add.html to link car-add-enhanced.js or merge code


============================================================================
DATABASE: No schema changes required
============================================================================

The Car entity already has:
   ✓ vin
   ✓ engineNo
   ✓ purchaseDate
   ✓ supplierInfo
   ✓ showroom (ManyToOne relationship)
   ✓ imageUrls (ElementCollection)

All fields are ready to use!


============================================================================
TESTING CHECKLIST:
============================================================================

After implementing the backend changes:

□ Navigate to /cars/add
□ Form should show all fields including VIN, Engine No, Purchase Date, Supplier, Showroom
□ Fill form with all required fields (marked with *)
□ Select showroom from dropdown
□ Add images (drag & drop should work)
□ Click "Add Vehicle"
□ Should redirect to /cars/showroom/{id} showing newly added car
□ Car should display in showroom-specific view
□ All car details (VIN, Engine No, etc.) should be visible


============================================================================
QUICK REFERENCE - METHOD SIGNATURES:
============================================================================

Car Model Fields to Set:
   car.setBrand(string)
   car.setModel(string)
   car.setVariant(string)
   car.setColor(string)
   car.setFuelType(string)
   car.setTransmission(string)
   car.setPrice(double)
   car.setVin(string)                    // NEW
   car.setEngineNo(string)                // NEW
   car.setPurchaseDate(LocalDate)         // NEW
   car.setSupplierInfo(string)            // NEW
   car.setShowroom(Showroom)              // NEW
   car.setImageUrls(List<String>)
   car.setStockQuantity(int)
   car.setStatus(string)


============================================================================
COMMON ISSUES & SOLUTIONS:
============================================================================

Issue: "Cannot resolve symbol ShowroomRepository in CarController"
Solution: Add @Autowired ShowroomRepository or inject via CarService

Issue: "LocalDate cannot be resolved"
Solution: Add import java.time.LocalDate to DashboardRestController

Issue: "Showroom dropdown is empty"
Solution: Verify showroomRepository.findAll() returns data in GET /cars/add

Issue: "Car not appearing in showroom view"
Solution: Verify showroom_id is being saved to car in database


============================================================================
