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
import glob
os.remove(glob.glob('beecloud-*.jar')[0])
os.remove(glob.glob('../demo_eclipse/libs/beecloud-*.jar')[0])
shutil.copy(glob.glob('build/libs/beecloud-*.jar')[0], '../demo_eclipse/libs')
shutil.copy(glob.glob('build/libs/beecloud-*.jar')[0], '.')

print('done')