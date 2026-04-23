v1.0.4
- Fixes issue where h2 DB connections don't always properly close on shutdown.
- Relocates most DB drivers but h2 to help avoid any issues with any other mods shipping those DB drivers as well.