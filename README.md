# PitNudge Android

**v0.7.0 Test Build — GPS Trip Route Maps**

Adds an optional **Save trip route maps** setting to the existing GPS mileage tracker. After enabled, newly tracked trips store a bounded, simplified array of GPS coordinates (up to 240 points per trip). The route can be viewed from **Trips → View map**, showing the path over an OpenStreetMap street map (when online), plus a local route-shape preview. Live trips have a **View live map** button. Older trips from before this release have only mileage and cannot be reconstructed.

**Privacy:** Route capture is OFF by default. It runs only during an opted-in active mileage service session; toggling the route option while tracking takes effect on the current trip. If cloud sync is active, trip routes are included in the encrypted-transport Firebase account backup along with other trip records, subject to the current size limits. Opening a street map makes tile requests to OpenStreetMap servers, which can see the map areas viewed. Users can remove the saved route for any single trip without changing its mileage. For a commercial/public launch, use a dedicated tile provider and review OSM tile rules and privacy policy.

**Mileage verification:** GPS estimates do not equal dashboard odometer readings. The Edit button lets users correct recorded trip miles and adjusts estimated mileage by the difference. GPS outliers are rejected, and route sampling is capped. While tracking with screen locked, Android shows a foreground notification using the custom tracking icon.

The workflow rebuilds the baseline source archive, then applies the Firebase/photo (v0.3), native mileage (v0.5), tracking notification icon (v0.6), and optional route-map (v0.7) overlays. Original PitNudge launcher artwork is unchanged. A Google sign-in test has succeeded previously; Android GPS route capture still requires a real device test. Keep backups before changing test builds.

Leaflet 1.9.4 (BSD 2-Clause license) is loaded from unpkg when a user opens a map; OpenStreetMap tiles require © OpenStreetMap contributors attribution. The app shows a local sketch if the street layer cannot load.
