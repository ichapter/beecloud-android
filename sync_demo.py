#根据demo文件同步demo_eclipse

import shutil
import os
import zipfile

def sync(src, dest):
	print("sync " + dest)
	if os.path.isfile(dest):
		os.remove(dest)
		shutil.copyfile(src, dest)
	elif os.path.isdir(dest):
		shutil.rmtree(dest)
		shutil.copytree(src, dest)
	else:
		print("no such file or dir...")


def zipdir(zipname, path):
	zipf = zipfile.ZipFile(zipname, 'w')

	for root, dirs, files in os.walk(path):
		if root.find('build') == -1 and root.find('bin') == -1:
			for file in files:
				zipf.write(os.path.join(root, file))

	zipf.close()


print('backup demo')
zipdir('demo.zip', 'demo/')

print('backup demo_eclipse')
zipdir('demo_eclipse.zip', 'demo_eclipse/')

print('synchronizing...')

#sync AndroidManifest
sync('demo/src/main/AndroidManifest.xml', 'demo_eclipse/AndroidManifest.xml')
#sync code
sync('demo/src/main/java', 'demo_eclipse/src')
#sync res
sync('demo/src/main/res', 'demo_eclipse/res')

print('done')