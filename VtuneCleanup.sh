#!/bin/bash
echo "Cleaning up VTune processes and temporary files..."
sudo pkill vtune
sudo rm -rf /tmp/vtune* /tmp/amplxe*
echo "Cleanup complete."