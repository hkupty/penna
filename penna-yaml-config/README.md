# maple-yaml-config

This is a very simple wrapper implementation of `maple.api.config.ConfigManager` that looks up for a `maple.yaml` file
in your resources and applies the configuration from that file into maple.

Given `maple` is designed for apps running in [kubernetes](https://kubernetes.io), we believe that yaml is a more
natural configuration format than xml, so it should be convenient to set yaml files in k8s.

## Roadmap

- 1.0
- [x] read `maple.yaml`;
- [ ] allow for a separate yaml file for test;
- [ ] watch `maple.yaml` file for re-configuring the app upon app update;
