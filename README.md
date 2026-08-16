# @wefterjs/network

Official Wefter plugin for querying real-time connectivity status and listening to network state changes on Android (`ConnectivityManager`) and iOS (`NWPathMonitor`).

---

## Features

- 🌐 **Network Status Inspection**: Query active connection state and interface type (`wifi`, `cellular`, `ethernet`, `none`) with `getStatus()`.
- 📡 **Real-Time Event Listener**: Subscribe to `onStatusChange` callbacks when connection toggles online/offline or switches transport.
- ⚡ **Offline-First Ready**: Zero background polling overhead; relies on native OS push callbacks (`NetworkCallback` / `NWPathMonitor`).

---

## Installation & Setup

1. Add the plugin to your Wefter project:

```bash
wefter add @wefterjs/network
```

2. Synchronize native projects:

```bash
wefter sync
```

---

## JavaScript / TypeScript API Reference

```ts
import { Network } from "@wefterjs/network";
```

### 1. `getStatus()`

Returns current network connectivity status.

```ts
interface NetworkStatus {
  connected: boolean;
  connectionType: "wifi" | "cellular" | "ethernet" | "bluetooth" | "vpn" | "unknown" | "none";
}

const status = await Network.getStatus();
console.log("Connected:", status.connected, "Type:", status.connectionType);
```

### 2. `onStatusChange(callback)`

Subscribes to network status updates. Returns a listener object with a `remove()` cleanup function.

```ts
const listener = Network.onStatusChange((status) => {
  if (status.connected) {
    console.log("Back online via", status.connectionType);
  } else {
    console.warn("Device went offline");
  }
});

// To unsubscribe:
listener.remove();
```

---

## Complete Usage Example

```ts
import { Network } from "@wefterjs/network";

export class NetworkObserver {
  private listener?: { remove(): void };

  async start(): Promise<void> {
    const initial = await Network.getStatus();
    this.updateUI(initial);

    this.listener = Network.onStatusChange((status) => {
      this.updateUI(status);
    });
  }

  stop(): void {
    this.listener?.remove();
  }

  private updateUI(status: { connected: boolean; connectionType: string }): void {
    const banner = document.getElementById("offline-banner");
    if (banner) {
      banner.style.display = status.connected ? "none" : "block";
    }
  }
}
```
