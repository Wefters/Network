import Foundation
import Network

final class NetworkPlugin: WefterPlugin {

    private lazy var monitor: NWPathMonitor = {
        let m = NWPathMonitor()
        m.pathUpdateHandler = { [weak self] path in
            guard let self = self else { return }
            self.currentPath = path
            let status = self.statusDict(for: path)
            self.emit("network:changed", status)
        }
        m.start(queue: monitorQueue)
        return m
    }()

    private let monitorQueue = DispatchQueue(label: "dev.wefter.plugins.network")
    private var currentPath: NWPath?

    private func ensureMonitoring() {
        _ = monitor
    }

    // @WefterMethod
    func getStatus(payload: [String: Any], callback: @escaping (Result<Any, Error>) -> Void) throws {
        ensureMonitoring()
        let path = currentPath ?? monitor.currentPath
        let status = statusDict(for: path)
        resolve(callback, data: status)
    }

    private func statusDict(for path: NWPath) -> [String: Any] {
        let isConnected = path.status == .satisfied
        let connectionType: String
        if !isConnected {
            connectionType = "none"
        } else if isVPN(path) {
            connectionType = "vpn"
        } else if path.usesInterfaceType(.wifi) {
            connectionType = "wifi"
        } else if path.usesInterfaceType(.cellular) {
            connectionType = "cellular"
        } else if path.usesInterfaceType(.wiredEthernet) {
            connectionType = "ethernet"
        } else {
            connectionType = "unknown"
        }

        return [
            "connected": isConnected,
            "connectionType": connectionType
        ]
    }

    private func isVPN(_ path: NWPath) -> Bool {
        let vpnPrefixes = ["utun", "tun", "tap", "ppp", "ipsec"]
        return path.availableInterfaces.contains { iface in
            vpnPrefixes.contains { iface.name.hasPrefix($0) }
        }
    }
}
