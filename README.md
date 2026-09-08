# @wefterjs/network

Official Wefter plugin for querying real-time connectivity status and listening to network state changes on Android and iOS.

## Features

- Query online connectivity state and transport type (Wi-Fi, cellular, ethernet, bluetooth, VPN, unknown, or none).
- Subscribe to real-time connection state change notifications without background polling.
- Zero battery overhead, using native push callbacks from the host operating system.

## Installation and setup

Install the plugin package in your Wefter application:

```bash
wefter add @wefterjs/network
wefter sync
```

### Native permissions

When synchronized:

- Android automatically requests `<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />`.
- iOS requires no additional permission keys.

## JavaScript API reference

Import `Network` from `@wefterjs/network`:

```ts
import { Network } from "@wefterjs/network";
```

### Check network status

```ts
const status = await Network.getStatus();

console.log("Is connected:", status.connected);
console.log("Connection type:", status.connectionType);
// "wifi", "cellular", "ethernet", "bluetooth", "vpn", "unknown", or "none"
```

### Subscribe to network changes

```ts
const sub = Network.onStatusChange((status) => {
  if (status.connected) {
    console.log("Online via:", status.connectionType);
  } else {
    console.log("Device is offline");
  }
});

// To unsubscribe:
sub.remove();
```

## Complete usage example

```ts
import { Network, type NetworkStatus } from "@wefterjs/network";

export class ConnectivityService {
  private listener?: { remove(): void };

  async initialize(onOffline: () => void, onOnline: (type: string) => void) {
    const current = await Network.getStatus();
    this.handleStatus(current, onOffline, onOnline);

    this.listener = Network.onStatusChange((status) => {
      this.handleStatus(status, onOffline, onOnline);
    });
  }

  private handleStatus(
    status: NetworkStatus,
    onOffline: () => void,
    onOnline: (type: string) => void
  ) {
    if (status.connected) {
      onOnline(status.connectionType);
    } else {
      onOffline();
    }
  }

  destroy() {
    this.listener?.remove();
  }
}
```

## Platform implementation notes

### Android

- Uses `android.net.ConnectivityManager`.
- Registers a `ConnectivityManager.NetworkCallback` with a `NetworkRequest` to receive notifications when networks become available, lost, or capabilities change.
- Maps `NetworkCapabilities.TRANSPORT_*` flags into unified connection type strings.

### iOS

- Uses Apple's `Network.framework` and `NWPathMonitor`.
- Starts monitoring on a dedicated background dispatch queue.
- Inspects `NWPath.status` (`.satisfied` vs `.unsatisfied`) and interfaces (`.wifi`, `.cellular`, `.wiredEthernet`).

## License

[MIT](LICENSE) © 2026 Sandip Ghimire
