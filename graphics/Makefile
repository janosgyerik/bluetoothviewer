# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
src_dir:=src
xhdpi_dir:=../bluetoothviewer/src/main/res/drawable-xhdpi
hdpi_dir:=../bluetoothviewer/src/main/res/drawable-hdpi
ldpi_dir:=../bluetoothviewer/src/main/res/drawable-ldpi
mdpi_dir:=../bluetoothviewer/src/main/res/drawable-mdpi

names:=$(patsubst $(src_dir)/%,%,$(wildcard $(src_dir)/*.png))
xhdpi_target:=$(patsubst %,$(xhdpi_dir)/%,$(names))
hdpi_target:=$(patsubst %,$(hdpi_dir)/%,$(names))
ldpi_target:=$(patsubst %,$(ldpi_dir)/%,$(names))
mdpi_target:=$(patsubst %,$(mdpi_dir)/%,$(names))
launchpad_target:=launchpad/icon.png launchpad/logo.png launchpad/brand.png
market_target:=market/android.png

default: help

help:
	@cat Makefile.readme

xhdpi: $(xhdpi_target)

hdpi: $(hdpi_target)

ldpi: $(ldpi_target)

mdpi: $(mdpi_target)

lp launchpad: $(launchpad_target)

market: $(market_target)

all: xhdpi hdpi ldpi mdpi launchpad market

clean: 
	rm $(xhdpi_target) $(hdpi_target) $(ldpi_target) $(mdpi_target) $(launchpad_target) $(market_target)

$(xhdpi_dir)/toolbar_%.png: $(src_dir)/toolbar_%.png
	convert -geometry 96x $< $@
	identify $@

$(hdpi_dir)/toolbar_%.png: $(src_dir)/toolbar_%.png
	convert -geometry 72x $< $@
	identify $@

$(ldpi_dir)/toolbar_%.png: $(src_dir)/toolbar_%.png
	convert -geometry 36x $< $@
	identify $@

$(mdpi_dir)/toolbar_%.png: $(src_dir)/toolbar_%.png
	convert -geometry 48x $< $@
	identify $@

$(xhdpi_dir)/launcher_%.png: $(src_dir)/launcher_%.png
	convert -geometry 96x $< $@
	identify $@

$(hdpi_dir)/launcher_%.png: $(src_dir)/launcher_%.png
	convert -geometry 72x $< $@
	identify $@

$(ldpi_dir)/launcher_%.png: $(src_dir)/launcher_%.png
	convert -geometry 36x $< $@
	identify $@

$(mdpi_dir)/launcher_%.png: $(src_dir)/launcher_%.png
	convert -geometry 48x $< $@
	identify $@

launchpad/icon.png: $(src_dir)/launcher_main.png
	@mkdir -p $(@D)
	convert -geometry 14x $< $@
	identify $@

launchpad/logo.png: $(src_dir)/launcher_main.png
	@mkdir -p $(@D)
	convert -geometry 64x $< $@
	identify $@

launchpad/brand.png: $(src_dir)/launcher_main.png
	@mkdir -p $(@D)
	convert -geometry 192x $< $@
	identify $@

market/android.png: $(src_dir)/launcher_main.png
	@mkdir -p $(@D)
	convert -geometry 512x $< $@
	identify $@
