# MostMedia SDK

A lightweight SDK for redirecting advertisement requests to a new server while maintaining full compatibility with the existing TagMarshal Golf app.

## Overview

The MostMedia SDK intercepts advertisement API calls and redirects them to a new server endpoint while preserving all existing functionality, data formats, and user experience.

## Features

- **Zero Breaking Changes**: Existing ad display logic remains unchanged
- **Seamless Integration**: Drop-in replacement for ad requests
- **Fallback Support**: Automatically falls back to original API if SDK is not configured
- **Same Data Format**: Returns advertisements in the exact same format as the original API
- **Easy Configuration**: Simple enable/disable functionality

## Configuration

### 1. Configure New Ad Server (Inside SDK)

Edit `MostMediaConfig.java`:

```java
// NEW AD SERVER URL - CONFIGURE HERE
private static final String NEW_AD_SERVER_URL = "https://your-new-ad-server.com/";

// Enable/disable the SDK - set to true to use new server
private static final boolean ENABLE_SDK = true; // Change to true to enable
```

### 2. Runtime Configuration (Optional)

```java
// Enable ad redirection at runtime
MostMediaHelper.enableAdRedirection("https://your-new-ad-server.com/");

// Disable ad redirection at runtime
MostMediaHelper.disableAdRedirection();
```

### 3. Check Status

```java
// Check if redirection is active
boolean isActive = MostMediaHelper.isAdRedirectionActive();

// Get current ad server URL
String currentServer = MostMediaHelper.getCurrentAdServer();
```

## Integration

The SDK is automatically integrated into the app's ad flow. When configured, it will:

1. Intercept calls to `getAdvertisements()`
2. Redirect them to the new server endpoint
3. Return the response in the original format
4. Allow all existing ad logic to work unchanged

## API Endpoint

The new server should implement a `GET /ads` endpoint that returns advertisements in the same format as the original TagMarshal API.

## Files Modified

The integration required minimal changes to only 2 existing files:

1. `FragmentMapModel.java` - Added SDK check in `getAdvertisements()`
2. `FirebaseModel.java` - Added SDK check in `getAdvertisements()`

**No configuration needed in app code** - everything is configured inside the SDK.

## Enhanced Banner Rotation

The SDK now includes enhanced banner rotation capabilities to fix issues where banners from different advertisements don't rotate properly.

### Problem Solved

- **Original Issue**: Banners were cycling through images of a single advertisement instead of different advertisements
- **Solution**: SDK provides utilities to properly handle rotation across multiple advertisements and their images

### Usage for Enhanced Banner Rotation

```java
// Check if enhanced rotation should be used
if (MostMediaHelper.shouldUseEnhancedRotation(bannersAdverts)) {
    // Create enhanced adapter
    EnhancedBannerAdapter enhancedAdapter = MostMediaHelper.createEnhancedBannerAdapter(context, bannersAdverts);
    bannersViewPager.setAdapter(enhancedAdapter);
    
    // Use enhanced rotation helper for timing
    int totalBanners = MostMediaSDK.getTotalBannerCount(bannersAdverts);
    long displayTime = MostMediaSDK.EnhancedRotationHelper.getDisplayTimeMs(bannersAdverts, currentIndex);
    int nextIndex = MostMediaSDK.EnhancedRotationHelper.getNextBannerIndex(currentIndex, totalBanners);
}
```

### Enhanced Rotation Features

- **Multiple Advertisement Support**: Properly rotates through banners from different advertisements
- **Individual Display Times**: Each banner can have its own display time
- **Smart Indexing**: Global banner indexing across all advertisements
- **Seamless Integration**: Drop-in replacement for existing banner adapters

## SDK Components

- `MostMediaSDK.java` - Main SDK interface with enhanced rotation utilities
- `MostMediaConfig.java` - Configuration management
- `MostMediaService.java` - Retrofit service interface
- `MostMediaHelper.java` - Simple configuration helper with enhanced methods
- `EnhancedBannerAdapter.java` - Advanced banner adapter for proper rotation

## Benefits

- **Minimal Code Changes**: Only 3 lines of actual changes to existing code
- **No Functionality Loss**: All existing ad features work exactly the same
- **Easy Testing**: Can be enabled/disabled at runtime
- **Clean Architecture**: SDK is self-contained and doesn't interfere with other features
- **Enhanced Banner Rotation**: Fixes banner rotation issues across multiple advertisements
- **Individual Banner Timing**: Each banner respects its own displayTime setting 