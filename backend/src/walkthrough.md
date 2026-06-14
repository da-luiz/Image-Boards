# Walkthrough - MVP Milestone Verification & Testing Guide

This guide summarizes the corrections applied to the codebase on the branch `feature/mvp-fixes` and provides a step-by-step guide to test the MVP Milestone.

---

## Changes Implemented

1. **Relocated `ImageService.kt`**:
   Moved the service file from the package root directly into the correct directory structure matching its package definition: [ImageService.kt](file:///PATH/Image-Boards/backend/src/main/kotlin/com/moodboard/backend/service/ImageService.kt).

2. **Seeded Initial Database Categories**:
   Created a new Flyway migration script [V2__seed_categories.sql](file:///PATH/Image-Boards/backend/src/main/resources/db/migration/V2__seed_categories.sql) to populate categories (Tech, Nature, Architecture, Aesthetics) so that images can be uploaded without throwing database foreign key violations.

3. **Served Uploaded Files Web-wide**:
   Created [WebConfig.kt](file:///PATH/Image-Boards/backend/src/main/kotlin/com/moodboard/backend/config/WebConfig.kt) implementing `WebMvcConfigurer`. It maps the `/files/**` URL pattern directly to your local `uploads/` directory, resolving the 404 error when attempting to access uploaded files.

4. **Corrected Endpoints Paths**:
   Updated [ImageController.kt](file:///PATH/Image-Boards/backend/src/main/kotlin/com/moodboard/backend/controller/ImageController.kt) with class-level `@RequestMapping("/api/images")` and adjusted endpoints. This aligns the routes with the requested URL format: `/api/images/upload`, `/api/images/latest`, and `/api/images`.

---

## Testing the MVP Milestone

Follow these steps to run the application and test the system end-to-end:

### Step 1: Start the Spring Boot Application
Since you chose Spring Boot's automatic Docker Compose integration, starting the Spring Boot app will automatically launch PostgreSQL.

1. **Open the Project in IntelliJ**:
    * Open IntelliJ IDEA.
    * Go to **File** -> **Open** and select the `/PATH/Image-Boards/backend` directory.
    * Wait a few moments for IntelliJ to sync and download the Gradle dependencies (you will see a progress bar at the bottom right).
2. **Ensure Docker is Running**:
    * Open **Docker Desktop** on your Mac and make sure it is running in the background.
3. **Run the Application**:
    * In IntelliJ's file tree on the left, navigate to `src` -> `main` -> `kotlin` -> `com.moodboard.backend` -> [BackendApplication.kt](file:///PATH/Image-Boards/backend/src/main/kotlin/com/moodboard/backend/BackendApplication.kt).
    * Open the file and locate the main function `fun main(args: Array<String>)` (line 9).
    * Click the green play icon (run button) in the editor margin directly to the left of `fun main` and choose **Run 'BackendApplicationKt'**.
4. **Keep it Running**:
    * The IntelliJ run console at the bottom will display startup logs. Once it finishes starting up, keep this running in the background. The server is now listening for requests on `http://localhost:8080`.
    * Leave it active and move on to Step 2.

*Alternatively, if you have a local JDK configured in your terminal:*
```bash
cd backend
./gradlew bootRun
```

---

### Step 2: Upload an Image via Postman
We need to trigger an upload and verify the response.

1. Open **Postman** and create a new request:
    * **Method**: `POST`
    * **URL**: `http://localhost:8080/api/images/upload?categoryId=1`
2. Go to the **Body** tab and select **form-data**:
    * Add a key named `file` of type **File**. Select any local `.jpg` or `.png` image.
    * Add a key named `categoryId` of type **Text**. Set the value to `1` (which matches the pre-seeded `Tech` category).
3. Click **Send**.
4. You should receive a `201 Created` response containing the image metadata and URL:
   ```json
   {
       "id": 1,
       "url": "http://localhost:8080/files/<uuid-generated-filename>.jpg",
       "categoryId": 1,
       "contentType": "image/jpeg",
       "width": null,
       "height": null,
       "createdAt": "2026-06-13T20:17:00Z"
   }
   ```

---

### Step 3: Verify the File Appears on Disk
1. Check the project root directory in your IDE or terminal.
2. A folder named `uploads` should have been created under `backend/`.
3. Inside, you should find a file named `<uuid-generated-filename>.jpg` (or `.png`).

---

### Step 4: Verify the Record in PostgreSQL
You can check if the metadata record was successfully saved in the PostgreSQL database using a DB client (like DBeaver or IntelliJ's Database tool) connected to:
* **Host**: `localhost`
* **Port**: `5432`
* **Database**: `moodboard`
* **Username**: `moodboard`
* **Password**: `localdev`

Run the query:
```sql
SELECT * FROM images WHERE id = 1;
```
It should return the row matching the metadata of the image you just uploaded!

---

### Step 5: Test GET `/api/images/latest`
We need to verify that the retrieval endpoint works.

1. In **Postman**, create a new request:
    * **Method**: `GET`
    * **URL**: `http://localhost:8080/api/images/latest`
2. Click **Send**.
3. You should see a list of images (including the one you just uploaded) with the correct `url` values:
   ```json
   [
       {
           "id": 1,
           "url": "http://localhost:8080/files/<uuid-generated-filename>.jpg",
           "categoryId": 1,
           "contentType": "image/jpeg",
           "width": null,
           "height": null,
           "createdAt": "2026-06-13T20:17:00Z"
       }
   ]
   ```

---

### Step 6: View the Image in a Web Browser
1. Copy the value of the `url` key from the JSON response (e.g. `http://localhost:8080/files/<uuid-generated-filename>.jpg`).
2. Open a web browser (e.g. Chrome, Safari, Firefox) and paste the URL in the address bar.
3. Press **Enter**. The image you uploaded should render on screen!
