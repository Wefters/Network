import { definePlugin, registerHook } from "@wefterjs/core";

export type ConnectionType =
  | "wifi"
  | "cellular"
  | "ethernet"
  | "bluetooth"
  | "vpn"
  | "unknown"
  | "none";

export interface NetworkStatus {
  connected: boolean;
  connectionType: ConnectionType;
}

const NativeNetwork = definePlugin<{
  getStatus: () => Promise<NetworkStatus>;
}>("network", {
  getStatus: true,
});

export const Network = {
  ...NativeNetwork,
  onStatusChange(callback: (status: NetworkStatus) => void): { remove(): void } {
    return registerHook("network:changed", callback as (data: unknown) => void);
  },
};
