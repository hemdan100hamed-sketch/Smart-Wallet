# Smart Wallet Version History

## Stable baseline (un-numbered reference)
- Status: Successfully built and tested by the user on Android 14.
- Verified behavior: prepares the complete Vodafone Cash transfer USSD code and opens the Android dialer with ACTION_DIAL; the user manually presses Call and completes the official flow.
- Repository: https://github.com/hemdan100hamed-sketch/Smart-Wallet
- Branch: `transfer-prefill-ui`
- Commit: `ff23b3267ff19f1378ba2dd79347d267761ffd2c`
- Archive: `Smart_Wallet_Transfer_Success_Android14.zip`
- APK artifact: `Smart-Wallet-debug`
- Rule: keep this baseline unchanged; all new work goes on a separate branch.

## Version 3.9 — Manual transfer-success receipt (in progress)
- Starting point: the stable baseline commit above.
- Working branch: `feature/manual-transfer-receipt-v3.9`
- Android versionName: `3.9`
- Android versionCode: `39`
- Planned behavior:
  - After the user independently sees a successful transfer confirmation from the wallet/network, they can manually confirm success in Smart Wallet.
  - Display only the receipt design in the app; no explanatory text or extra controls outside the receipt.
  - The receipt's in-app design follows the user's supplied reference image as closely as possible.
  - The in-app receipt includes its Share control.
  - Sharing exports the same receipt image with the Share control/word removed.
  - Amount and recipient number are populated from the transfer details entered by the user.
  - Clearly treat this as a user-generated shareable record, not an official network-issued receipt or independent verification.
- Implemented in source (first pass): manual confirmation dialog after transfer preparation; local record screen with dynamic amount/recipient; Share action renders the record view to PNG and opens Android Sharesheet through FileProvider; exported image excludes the in-app Share button.
- Safety distinction: record screen labels it as a locally generated Smart Wallet record based on user confirmation, not a wallet-provider-issued receipt.
- Visual status: structural first pass only; exact pixel-level matching is not claimed. The linked reference image was viewable, but its pixels were not available as a directly attached source asset for precise measurement.
- Build status: not yet built in this environment; Android device testing not yet completed.
- Remaining validation: compile on CI/Android Studio; test dialer prefill, manual confirmation, dynamic fields, generated PNG, and sharing to WhatsApp/Telegram; compare screen against the reference image on-device.


## Version 3.10 — Restore full wallet app and e& Cash manual record (in progress)
- Working branch: `feature/manual-transfer-receipt-v3.10`
- Android versionName: `3.10`
- Android versionCode: `40`
- Starting source for the full application flows: `smart-wallet-v3.6`, preserving both Vodafone Cash and e& Cash services, balance inquiry, contacts, favorites, and PIN masking/clearing behavior.
- New feature is scoped to e& Cash only: after entering transfer details and preparing the e& Cash USSD transfer, the user can manually confirm success and view/share a local record populated with the entered amount and recipient.
- Vodafone Cash transfer flow remains unchanged from the restored 3.6 source; no receipt button or receipt workflow is added to Vodafone Cash.
- Receipt is a locally generated Smart Wallet record based on user confirmation, not a provider-issued receipt or automatic network verification. The Share action exports the receipt card only, without the in-app Share button.
- Original launcher icon resources and manifest icon declarations are restored.
- FileProvider remains configured for PNG sharing.
- Build status: source changes committed; CI build and APK artifact not yet verified.
- Device testing: not yet performed. Required tests: both wallet selection/services, Vodafone balance and transfer, e& balance and transfer/PIN flow, manual confirmation, receipt fields, PNG export/share, and launcher icon on device.
