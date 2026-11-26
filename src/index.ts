/** Reports network connectivity and changes to it. */

import { definePlugin, registerHook } from "@wefterjs/core";

/** The kind of connection in use. `none` means offline. */
export type ConnectionType = "wifi" | "cellular" | "ethernet" | "bluetooth" | "vpn" | "unknown" | "none";

/** Current connectivity. */
export interface NetworkStatus {
  connected: boolean;
  connectionType: ConnectionType;
}

/** Native methods of the plugin. */
const NativeNetwork = definePlugin<{
  /** The current connection status. */
  getStatus: () => Promise<NetworkStatus>;
}>("network", {
  getStatus: true,
});

/** The Network plugin. */
export const Network = {
  ...NativeNetwork,
  /**
   * Calls `callback` whenever connectivity changes.
   *
   * @returns A handle whose `remove()` stops listening.
   */
  onStatusChange(callback: (status: NetworkStatus) => void): { remove(): void } {
    return registerHook("network:changed", callback as (data: unknown) => void);
  },
};
