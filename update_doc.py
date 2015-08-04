#用于编译sdk，生成jar，并更换demo_eclipse中的beecloud.jar
#依赖gradle

#compile sdk
import os

os.chdir('sdk')

print('generating javadoc...')

import subprocess

rc = subprocess.call('gradle generateReleaseJavaDoc', shell=True)

if rc != 0:
	print('error when trying to generate javadoc...')
	exit(1)

print('copying...')

import shutil
shutil.rmtree('../doc')
shutil.copytree('build/docs/javadoc/', '../doc')

print('done')