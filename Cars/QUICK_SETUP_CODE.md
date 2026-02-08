============================================================================
BACKEND CODE - QUICK COPY-PASTE GUIDE
============================================================================

COPY-PASTE THESE CODE BLOCKS INTO YOUR BACKEND FILES

============================================================================
FILE 1: CarController.java
============================================================================

// Add this import at the top
import com.Dk3.Cars.repository.CarRepository;
import com.Dk3.Cars.repository.ShowroomRepository;

// Add these fields in the class (after carService)
@Autowired
private ShowroomRepository showroomRepository;

@Autowired
private CarRepository carRepository;

// REPLACE the existing GET /cars/add method with this:
@GetMapping("/add")
public String showAddCarForm(Model model) {
    model.addAttribute("showrooms", showroomRepository.findAll());
    return "car-add-final"; // Options: "car-add-final", "car-add", or "car-add-v2"
}

// ADD this new method after the /add method
@GetMapping("/showroom/{id}")
public String showCarsInShowroom(@PathVariable Long id, Model model) {
    model.addAttribute("cars", carRepository.findByShowroomIdOrderByIdDesc(id));
    model.addAttribute("showroomId", id);
    return "showroom-cars";
}


============================================================================
FILE 2: DashboardRestController.java (imports at top)
============================================================================

// Add this import with the other time imports (around line 15)
import java.time.LocalDate;


============================================================================
FILE 3: DashboardRestController.java (replace the addCar method)
============================================================================

// REPLACE the entire @PostMapping("/cars/add") method with this:

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


============================================================================
FILE 4: CarService.java (optional - add getter method)
============================================================================

// Add this method to expose CarRepository (if needed)
public CarRepository getCarRepository() {
    return carRepository;
}


============================================================================
TEMPLATE CHOICE - Pick ONE and update controller return value
============================================================================

Option A (Recommended - Best balance):
  return "car-add-final";

Option B (Premium UI):
  return "car-add-v2";

Option C (Keep existing):
  return "car-add"; // Update car-add.html with new content


============================================================================
FILE MAPPING - What each template file does
============================================================================

car-add.html (UPDATED)
  → Basic form with all fields
  → Uses car-add.css
  → Uses car-add.js or car-add-updated.js

car-add-final.html (NEW - BEST FOR QUICK SETUP)
  → Modern form with sections
  → Inline CSS (no extra file needed)
  → Uses car-add.js or car-add-updated.js

car-add-v2.html (NEW - PREMIUM)
  → Full featured modern design
  → References car-add-enhanced.css
  → Uses car-add-enhanced.js

showroom-cars.html (NEW - SHOWROOM VIEW)
  → Displays cars in specific showroom
  → Shows all car details
  → Automatically shown after car add


============================================================================
JAVASCRIPT CHOICE - Pick ONE
============================================================================

Option A (Minimal changes):
  Use existing car-add.js
  (Already has FormData fix)

Option B (Recommended - Better features):
  Rename car-add-updated.js → car-add.js
  (Handles all new fields, redirects to showroom)

Option C (Premium):
  Rename car-add-enhanced.js → car-add.js
  (Most features, works with car-add-enhanced.css)


============================================================================
CSS CHOICE - Pick ONE
============================================================================

Option A (Keep existing):
  Use car-add.css as is
  (Works with all templates)

Option B (Better styling):
  Rename car-add-enhanced.css → car-add.css
  (Modern design, responsive, animations)

Option C (Hybrid):
  Merge content from car-add-enhanced.css into car-add.css
  (Keep compatibility, add new styles)


============================================================================
MIGRATION CHECKLIST
============================================================================

□ Step 1: Stop the running application
□ Step 2: Backup current files (create a branch)
□ Step 3: Update CarController.java with new imports, fields, and methods
□ Step 4: Update DashboardRestController.java with new import
□ Step 5: Replace @PostMapping("/cars/add") method
□ Step 6: Choose and link template file (car-add-final.html recommended)
□ Step 7: Choose and link JavaScript file (car-add-updated.js recommended)
□ Step 8: Choose and link CSS file (car-add-enhanced.css or merge content)
□ Step 9: Compile the project (mvn clean compile)
□ Step 10: Run unit tests if any
□ Step 11: Start the application
□ Step 12: Test at http://localhost:8080/cars/add
□ Step 13: Verify form fields appear
□ Step 14: Verify showroom dropdown is populated
□ Step 15: Submit test form
□ Step 16: Verify redirect to /cars/showroom/{id}
□ Step 17: Verify car appears in showroom view
□ Step 18: Check database for new car record


============================================================================
COMMON MISTAKES TO AVOID
============================================================================

❌ Don't: Forget to add the new imports
   ✅ Do: Add all imports at the top of the file

❌ Don't: Forget the @Autowired annotations
   ✅ Do: Add @Autowired before each repository/service

❌ Don't: Keep the old @PostMapping method
   ✅ Do: Completely replace it with new version

❌ Don't: Forget LocalDate import
   ✅ Do: Add java.time.LocalDate import

❌ Don't: Use wrong template name in controller return
   ✅ Do: Use "car-add-final" or "car-add-v2" or "car-add"

❌ Don't: Forget to update the template in controller
   ✅ Do: Update return statement in GET /cars/add

❌ Don't: Forget the showroom GET method
   ✅ Do: Add GET /cars/showroom/{id} method

❌ Don't: Link JavaScript file that doesn't exist
   ✅ Do: Use existing or renamed file


============================================================================
IF YOU GET COMPILATION ERRORS:
============================================================================

Error: "Cannot resolve symbol 'ShowroomRepository'"
Fix: Add import com.Dk3.Cars.repository.ShowroomRepository;

Error: "Cannot resolve symbol 'LocalDate'"
Fix: Add import java.time.LocalDate;

Error: "showroom not found in method signature"
Fix: Make sure method signature includes: @RequestParam(value = "showroom", required = false) Long showroomId

Error: "CarRepository not injected"
Fix: Add: @Autowired private CarRepository carRepository;

Error: "Template not found: car-add-final"
Fix: Make sure file car-add-final.html exists in src/main/resources/templates/

Error: "POST /api/dashboard/cars/add not found"
Fix: Check that @PostMapping annotation is present over the correct method


============================================================================
VERIFICATION QUERIES FOR DATABASE
============================================================================

After adding a car, verify in database:

SELECT * FROM car WHERE brand = 'YourBrand' ORDER BY id DESC LIMIT 1;

Should show:
- id: auto-generated
- brand: your input
- model: your input
- variant: your input
- color: your input
- fuel_type: your input
- transmission: your input
- vin: your input
- engine_no: your input
- price: your input
- purchase_date: your date
- supplier_info: your input
- showroom_id: your selected showroom ID ✓ (MOST IMPORTANT)
- stock_quantity: your input
- status: "Available" (default)
- image_urls: comma-separated image URLs
- sold: false

To verify showroom assignment:
SELECT c.brand, c.model, s.name, s.city 
FROM car c 
LEFT JOIN showroom s ON c.showroom_id = s.id 
ORDER BY c.id DESC LIMIT 1;


============================================================================
