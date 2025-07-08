# Debugging Plan

This file summarizes the errors found in `Medium-Phone-API-36.0-Android-16_2025-07-07_155329.logcat` and proposes fixes. The logcat contains many system level errors alongside a few messages from the app `com.example.app`.

## App specific errors

- **ashmem – `Pinning is deprecated since Android Q`**
  - *Cause*: The app or one of its libraries tries to use ashmem pinning which was deprecated in Android Q.
  - *Fix*: Remove pinning calls or replace with modern alternatives such as `MemoryFile` or use trimming APIs.

- **Camera2CameraImpl – `Camera reopening attempted for 10000ms without success.`**
  - *Cause*: The camera could not be reopened, likely due to missing permissions or the camera being in use.
  - *Fix*: Ensure the camera permission is declared and granted at runtime. Release the camera properly before attempting to reopen.

## Frequent system errors

- **IPCThreadState – `Binder transaction failure`**
  - *Cause*: Binder calls sent with invalid arguments or insufficient permissions.
  - *Fix*: Verify binder transactions and permissions. For emulator errors this may be ignored.

- **Parcel – `Reading a NULL string not supported here.`**
  - *Cause*: Code tried to read a null string from a `Parcel` object.
  - *Fix*: Guard against null values before writing/reading from `Parcel`.

- **CameraService – `Permission Denial: can't use the camera`**
  - *Cause*: The calling process lacks camera permission.
  - *Fix*: Grant `android.permission.CAMERA` in the manifest and request runtime permission.

- **BluetoothPowerStatsCollector – `Cannot acquire BluetoothActivityEnergyInfo`**
  - *Cause*: The system could not obtain bluetooth power stats, often benign on emulators.
  - *Fix*: Ensure bluetooth hardware/services are available or ignore when running on an emulator.

- **Finsky – counter increment and ItemStore failures**
  - *Cause*: Play Store internal counters used invalid increments or RPC failures.
  - *Fix*: Usually harmless; update Google Play Services if persistent.

- **studio.deploy – `run-as: unknown package: com.example.app`**
  - *Cause*: Deploy/Apply Changes attempted without the app installed or with wrong package id.
  - *Fix*: Reinstall the debug build before using Apply Changes.

- **SmsApplication – lost `android:read_cell_broadcasts` permission**
  - *Cause*: Messaging app lost a runtime permission.
  - *Fix*: Grant the permission or update the app to handle permission revocation.

- **TaskPersister – `File error accessing recents directory`**
  - *Cause*: Recents directory missing on the device.
  - *Fix*: Usually safe to ignore on emulators; ensure the directory exists on real devices.

- **RoleControllerServiceImpl – fallback role holder package doesn't qualify**
  - *Cause*: System role delegation misconfiguration.
  - *Fix*: Update Google Play services or adjust device configuration.

- **ClipboardService – Denying clipboard access to Chrome**
  - *Cause*: Chrome attempted clipboard access without focus.
  - *Fix*: Not an app bug; ignore.

- **BluetoothAdapter – `Failed to retrieve a binder`**
  - *Cause*: Bluetooth feature not supported or disabled on emulator.
  - *Fix*: Ignore on emulator or ensure proper bluetooth services when running on hardware.

- **hpkp – `Previous channel was garbage collected without being shut down`**
  - *Cause*: gRPC channel was not properly closed.
  - *Fix*: Ensure all gRPC channels call `shutdown()` when done.

- **android.hardware.bluetooth.service.default – `unable to open /dev/rfkill`**
  - *Cause*: Missing permissions or feature not supported on emulator.
  - *Fix*: Ignore on emulator or run on device with bluetooth hardware.

- **SystemServiceRegistry – `No service published for: persistent_data_block`**
  - *Cause*: Service not present on the device (common on emulators).
  - *Fix*: Ignore on emulator or include the service on a full device image.

- **DialerRestVvmCarrierService – unexpected phone number format**
  - *Cause*: Dialer retrieved a phone number in an unsupported format.
  - *Fix*: Ensure phone numbers are parsed/normalized before use.

- **bluetooth – service android.hardware.bluetooth.socket.IBluetoothSocket/default not declared**
  - *Cause*: Bluetooth socket service missing on emulator.
  - *Fix*: Ignore on emulator; ensure full bluetooth stack on real device.

- **JobScheduler.JobStatus – app became active but still in NEVER bucket**
  - *Cause*: JobScheduler bucket state mismatch.
  - *Fix*: Usually benign; verify app standby buckets if impacting behavior.

- **AppOps – `attributionTag VCN not declared in manifest`**
  - *Cause*: System expecting a manifest tag not present.
  - *Fix*: Add the required attribution tag if using related APIs.

- **constellation – `GetConsent failed with PERMISSION_DENIED`**
  - *Cause*: Google Play services component missing permissions.
  - *Fix*: Ensure correct permissions or ignore on emulator.

- **android.hardware.audio@7.1-impl.ranchu – `pcmWrite failure`**
  - *Cause*: Audio output error on emulator.
  - *Fix*: Ignore or verify audio configuration.

- **adservices.measurement – job services disabled**
  - *Cause*: Measurement job service disabled in settings.
  - *Fix*: Enable the service if measurement is needed.

- **WifiStaIfaceAidlImpl – `setDtimMultiplier failed`**
  - *Cause*: Wi-Fi stack call failed on emulator.
  - *Fix*: Ignore or test on real device with Wi‑Fi hardware.

- **RecyclerView – `Cannot scroll to position`**
  - *Cause*: Attempted to scroll before setting a `LayoutManager`.
  - *Fix*: Always set a `LayoutManager` before scrolling.

- **NativeTombstoneManager – UID not an app**
  - *Cause*: Crash tombstone generated for non-app UID.
  - *Fix*: Typically safe to ignore unless debugging system processes.

- **MediaBrowser – `onConnectFailed`**
  - *Cause*: MediaBrowserService connection failure.
  - *Fix*: Ensure the target service exists and the client has permission.

- **BrowsedPlayerWrapper – Could not get folder items**
  - *Cause*: Media browsing service failed to return items.
  - *Fix*: Check connectivity or service implementation.

- **AccountManagerService – Could not determine packageUid**
  - *Cause*: Package name not found when querying AccountManager.
  - *Fix*: Verify the package exists and is installed.

- **tombstoned – `Tombstone written`**
  - *Cause*: Native crash recorded.
  - *Fix*: Review tombstone for the crashing native process.

- **InputDispatcher – Channel is unrecoverably broken**
  - *Cause*: Window for the app crashed or was closed unexpectedly.
  - *Fix*: Investigate UI thread stability and ensure window is properly managed.

- **Other minor errors (CCTFlatFileLogStore, BpTransactionCompletedListener, etc.)**
  - *Fix*: Usually related to system services; review individual stack traces if they impact app behavior.

## Next Steps

1. Prioritize fixing the app specific issues (`ashmem` and camera reopening problems`).
2. Review permission settings in the manifest to resolve camera denial and potential bluetooth or SMS permissions.
3. Test on a physical device to rule out emulator-specific errors.
4. Ignore or monitor remaining system errors unless they affect app functionality.
