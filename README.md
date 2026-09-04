<!-- pre39-port-header:start -->
> **NeoForge 1.21.1 · Hex Casting `0.12.0-devel-pre-39` · Branch `pre39`**
>
> **原项目 / Upstream:** [https://github.com/FallingColors/Hexal](https://github.com/FallingColors/Hexal)  
> **移植基准 / Base:** [`b0d90daa44ab278974dc735934b927478a6e84c7`](https://github.com/FallingColors/Hexal/commit/b0d90daa44ab278974dc735934b927478a6e84c7)  
> **许可证 / License:** [LICENSE.txt](LICENSE.txt)  
> **文档 / Documentation:** [移植说明](PORTING.md) · [上游原始文档、署名与版权清单](UPSTREAM.md)
>
> This is a NeoForge port maintained by FluorineUCK, not the original upstream release. Original authorship and license notices are retained. Loader/version/build instructions in inherited upstream text describe the upstream project; the current port baseline is listed above.
<!-- pre39-port-header:end -->

Installation information
=======

This template repository can be directly cloned to get you started with a new
mod. Simply create a new repository cloned from this one, by following the
instructions provided by [GitHub](https://docs.github.com/en/repositories/creating-and-managing-repositories/creating-a-repository-from-a-template).

Once you have your clone, simply open the repository in the IDE of your choice. The usual recommendation for an IDE is either IntelliJ IDEA or Eclipse.

If at any point you are missing libraries in your IDE, or you've run into problems you can
run `gradlew --refresh-dependencies` to refresh the local cache. `gradlew clean` to reset everything 
{this does not affect your code} and then start the process again.

Mapping Names:
============
By default, the MDK is configured to use the official mapping names from Mojang for methods and fields 
in the Minecraft codebase. These names are covered by a specific license. All modders should be aware of this
license. For the latest license text, refer to the mapping file itself, or the reference copy here:
https://github.com/NeoForged/NeoForm/blob/main/Mojang.md

Additional Resources: 
==========
Community Documentation: https://docs.neoforged.net/  
NeoForged Discord: https://discord.neoforged.net/
