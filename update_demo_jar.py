#用于编译sdk，生成jar，并更换demo_eclipse中的beecloud.jar
#依赖gradle

#compile sdk
import os

os.chdir('sdk')

print('compiling...')

import subprocess

rc = subprocess.call('gradle makeJar', shell=True)

if rc != 0:
	print('error when trying to make jar...')
	exit(1)

print('copying...')

import shutil
shutil.copy('build/libs/beecloud.jar', '../demo_eclipse/libs')

print('done')