"""
# Copyright 2017 FrogSquare. All Rights Reserved.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#       http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
"""

import os
from colors import *

# Set your Android app ID
p_app_id = "com.example.game"

def can_build(env_plat, plat = None):
    #return False
    if plat == None:
        #print("`GodotAds`"+RED+" master "+RESET+" branch not compatable with godot 2.X")
        #print("Try using `GodotAds` "+GREEN+" 2.X "+RESET+" branch for Godot 2.X")

        if isinstance(env_plat, basestring):
            plat = env_plat
        else:
            print("GodotGoogleService: "+RED+" Platform not set, Disabling GodotGoogleService "+RESET)
            return False

    if plat == "android":
        print("GodotGoogleService: " + GREEN + "Enabled" + RESET)
        return True
    else:
        print("GodotGoogleService: " + RED + "Disabled" + RESET)
        return False
    pass   

def implement(api, support=True):
    supportv4 = "{exclude group: 'com.android.support' exclude module: 'support-v4'}"
    return "implementation('"+api+"')" + (supportv4 if support else "")
    pass

def configure(env):
    global p_app_id
    if env["platform"] == "android":

        if env.get("application_id", None) != None:
            p_app_id = env["application_id"]

        env.android_add_maven_repository("url 'https://maven.google.com'")
        env.android_add_maven_repository("url 'https://oss.sonatype.org/content/repositories/snapshots'")

        env.android_add_gradle_classpath("com.google.gms:google-services:4.1.0")
        env.android_add_gradle_plugin("com.google.gms.google-services")

        env.android_add_dependency(implement("com.android.support:support-fragment:28.0.0", False))
        env.android_add_dependency(implement("com.google.android.gms:play-services-auth:16.0.1"))
        env.android_add_dependency(implement("com.google.android.gms:play-services-games:16.0.0"))

	env.android_add_dependency(implement("com.google.firebase:firebase-invites:16.1.0"))

        env.android_add_java_dir("android");
        env.android_add_res_dir("res");

        if "frogutils" in [os.path.split(path)[1] for path in env.android_java_dirs]: pass
        else: env.android_add_java_dir("frogutils");

        env.android_add_to_manifest("android/AndroidManifestChunk.xml");
        env.android_add_to_permissions("android/AndroidPermissionsChunk.xml");
        env.android_add_default_config("applicationId '"+p_app_id+"'")
        env.disable_module()

    pass
