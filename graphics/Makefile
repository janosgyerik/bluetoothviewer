# Copyright (C) 2011 Janos Gyerik
#
# This file is part of BluetoothViewer.
#
# BluetoothViewer is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# BluetoothViewer is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with BluetoothViewer.  If not, see <http://www.gnu.org/licenses/>.
#
src_dir:=src
hdpi_dir:=../res/drawable-hdpi
ldpi_dir:=../res/drawable-ldpi
mdpi_dir:=../res/drawable-mdpi

names:=$(patsubst $(src_dir)/%,%,$(wildcard $(src_dir)/*.png))
hdpi_target:=$(patsubst %,$(hdpi_dir)/%,$(names))
ldpi_target:=$(patsubst %,$(ldpi_dir)/%,$(names))
mdpi_target:=$(patsubst %,$(mdpi_dir)/%,$(names))

default:
	@cat Makefile.readme

hdpi: $(hdpi_target)

ldpi: $(ldpi_target)

mdpi: $(mdpi_target)

all: $(hdpi_target) $(ldpi_target) $(mdpi_target)

$(hdpi_dir)/%.png: $(src_dir)/%.png
	convert -geometry 72x $< $@
	identify $@

$(ldpi_dir)/%.png: $(src_dir)/%.png
	convert -geometry 36x $< $@
	identify $@

$(mdpi_dir)/%.png: $(src_dir)/%.png
	convert -geometry 48x $< $@
	identify $@

