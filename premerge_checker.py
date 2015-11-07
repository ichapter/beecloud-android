#校验realease version
print('checking version consistency...')

with open('sdk/build.gradle', encoding = 'utf-8') as f:
	for line in f:
		tmp = line.find('versionName')
		if tmp != -1:
			gradle_ver = line[line.find('"')+1:line.rfind('"')]
			break;

print("gradle versionCode: " + gradle_ver)


with open('sdk/src/main/java/cn/beecloud/BeeCloud.java', encoding = 'utf-8') as f:
	for line in f:
		tmp = line.find('BEECLOUD_ANDROID_SDK_VERSION')
		if tmp != -1:
			beecloud_ver = line[line.find('"')+1:line.rfind('"')]
			break;

print("BeeCloud versionCode: " + beecloud_ver)


with open('README.md', encoding = 'utf-8') as f:
	for line in f:
		tmp = line.find('version-v')
		if tmp != -1:
			readme_ver = line[line.find('version-v')+len('version-v')
				:line.rfind('-blue')]

			continue;

		tmp = line.find(r'sdk\beecloud-')
		if tmp != -1:
			readme_jar_ver = line[line.find(r'sdk\beecloud-')+len(r'sdk\beecloud-')
				:line.rfind('.jar')]

			break;


print("Readme versionCode: " + readme_ver)
print("Readme jar versionCode: " + readme_jar_ver)

with open('changelog.txt', encoding = 'utf-8') as f:
	for line in f:
		if line.startswith('v'):
			changelog_ver = line[1: line.find(' ')]
			break;

print("changelog versionCode: " + changelog_ver)


import glob

if len(glob.glob('sdk/beecloud*.jar')) != 1:
	print('dumy jars!!!')
	exit(-1)

tmp = glob.glob('sdk/beecloud*.jar')[0]
sdk_jar_ver = tmp[tmp.find('beecloud-') + len('beecloud-') : tmp.find('.jar')]
print("sdk jar versionCode: " + sdk_jar_ver)


if len(glob.glob('demo_eclipse/libs/beecloud*.jar')) != 1:
	print('dumy jars!!!')
	exit(-1)

tmp = glob.glob('demo_eclipse/libs/beecloud*.jar')[0]
demo_jar_ver = tmp[tmp.find('beecloud-') + len('beecloud-') : tmp.find('.jar')]
print("eclipse demo jar versionCode: " + demo_jar_ver)

if gradle_ver != beecloud_ver or beecloud_ver != readme_ver or readme_ver != readme_jar_ver or readme_jar_ver != changelog_ver or changelog_ver != sdk_jar_ver or sdk_jar_ver != demo_jar_ver:
	print("inconsistent version code!!!")
	exit(-1)
else:
	print("PASS\n\n")


#校验demo账号
print('checking demo account...')

with open('demo/src/main/java/cn/beecloud/demo/ShoppingCartActivity.java', encoding = 'utf-8') as f:
	for line in f:
		if line.find('BeeCloud.setAppIdAndSecret') != -1 and line.find('c37d661d') != -1:
			print('please check account!!!')
			exit(-2)
			

with open('demo_eclipse/src/cn/beecloud/demo/ShoppingCartActivity.java', encoding = 'utf-8') as f:
	for line in f:
		if line.find('BeeCloud.setAppIdAndSecret') != -1 and line.find('c37d661d') != -1:
			print('please check account!!!')
			exit(-2)

print("PASS\n\n")


#校验host
print('checking host...')

with open('sdk/src/main/java/cn/beecloud/BCHttpClientUtil.java', encoding = 'utf-8') as f:
	for line in f:
		if line.find('https') != -1 and line.find('beecloud') != -1:
			if line.strip() != r'private static final String[] BEECLOUD_HOSTS = {"https://apibj.beecloud.cn",' and line.strip() != r'"https://apisz.beecloud.cn",' and line.strip() != r'"https://apiqd.beecloud.cn",' and line.strip() != r'"https://apihz.beecloud.cn"':
				print("host init error!!!")
				exit(-3);

print("PASS")