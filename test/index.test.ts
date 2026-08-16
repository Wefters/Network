// @vitest-environment jsdom
import { afterEach, describe, expect, it, vi } from "vitest";
import { installMockBridge, uninstallMockBridge } from "@wefterjs/core/testing";
import { Network } from "../src/index.js";

afterEach(() => {
  uninstallMockBridge();
});

describe("Network.getStatus", () => {
  it("resolves online network status with wifi connection", async () => {
    installMockBridge({
      network: (method) => {
        expect(method).toBe("getStatus");
        return { connected: true, connectionType: "wifi" };
      },
    });

    const status = await Network.getStatus();
    expect(status).toEqual({ connected: true, connectionType: "wifi" });
  });

  it("resolves offline network status", async () => {
    installMockBridge({
      network: (method) => {
        expect(method).toBe("getStatus");
        return { connected: false, connectionType: "none" };
      },
    });

    const status = await Network.getStatus();
    expect(status).toEqual({ connected: false, connectionType: "none" });
  });
});

describe("Network.onStatusChange", () => {
  it("registers event hook for network:changed", () => {
    const callback = vi.fn();
    const handle = Network.onStatusChange(callback);
    expect(handle).toBeDefined();
    expect(typeof handle.remove).toBe("function");

    handle.remove();
  });
});
